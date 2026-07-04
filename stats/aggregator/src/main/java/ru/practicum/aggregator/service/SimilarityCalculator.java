package ru.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SimilarityCalculator {

    private static final String SIMILARITY_TOPIC = "stats.events-similarity.v1";

    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventTotalSums = new ConcurrentHashMap<>();

    private final MinWeightsMatrix minWeightsMatrix = new MinWeightsMatrix();

    private final KafkaTemplate<Void, EventSimilarityAvro> kafkaTemplate;

    public SimilarityCalculator(KafkaTemplate<Void, EventSimilarityAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = getWeight(action.getActionType());

        log.debug("Processing action: userId={}, eventId={}, weight={}", userId, eventId, newWeight);

        Map<Long, Double> userWeights = eventUserWeights.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        Double oldWeight = userWeights.get(userId);

        if (oldWeight != null && oldWeight >= newWeight) {
            log.debug("Weight not increased: userId={}, eventId={}, old={}, new={}",
                    userId, eventId, oldWeight, newWeight);
            return;
        }

        userWeights.put(userId, newWeight);

        double oldTotal = eventTotalSums.getOrDefault(eventId, 0.0);
        double delta = (oldWeight == null) ? newWeight : (newWeight - oldWeight);
        eventTotalSums.put(eventId, oldTotal + delta);

        log.debug("Updated total for event {}: {} -> {}", eventId, oldTotal, oldTotal + delta);

        for (Long otherEventId : eventUserWeights.keySet()) {
            if (otherEventId.equals(eventId)) continue;

            Map<Long, Double> otherUserWeights = eventUserWeights.get(otherEventId);
            Double otherWeight = otherUserWeights != null ? otherUserWeights.get(userId) : null;

            if (otherWeight != null) {
                double minWeight = Math.min(newWeight, otherWeight);
                minWeightsMatrix.add(eventId, otherEventId, minWeight);

                sendSimilarity(eventId, otherEventId);
            }
        }
    }

    private void sendSimilarity(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        double sMin = minWeightsMatrix.get(first, second);
        double sA = eventTotalSums.getOrDefault(first, 0.0);
        double sB = eventTotalSums.getOrDefault(second, 0.0);

        if (sA == 0 || sB == 0) {
            log.debug("Skipping similarity: sA={}, sB={}", sA, sB);
            return;
        }

        double norm1 = Math.sqrt(sA);
        double norm2 = Math.sqrt(sB);
        double similarity = sMin / (norm1 * norm2);
        similarity = Math.min(1.0, Math.max(0.0, similarity));

        log.info("Similarity: {} <-> {} = {} (sMin={}, norm1={}, norm2={})",
                first, second, similarity, sMin, norm1, norm2);

        EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore((float) similarity)
                .setTimestamp(Instant.ofEpochSecond(System.currentTimeMillis()))
                .build();

        kafkaTemplate.send(SIMILARITY_TOPIC, null, avro);
        log.info("Sent similarity to topic: {}", SIMILARITY_TOPIC);
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }

    private static class MinWeightsMatrix {
        private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

        public void add(long eventA, long eventB, double value) {
            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);
            minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                    .merge(second, value, Double::sum);
        }

        public double get(long eventA, long eventB) {
            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);
            return minWeightsSums.getOrDefault(first, Map.of())
                    .getOrDefault(second, 0.0);
        }
    }
}