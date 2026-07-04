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

    private final Map<Long, Map<Long, Double>> userEventWeights = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventTotalSums = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    private final KafkaTemplate<Void, EventSimilarityAvro> kafkaTemplate;

    public SimilarityCalculator(KafkaTemplate<Void, EventSimilarityAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double weight = getWeight(action.getActionType());

        log.info("Processing action: userId={}, eventId={}, weight={}", userId, eventId, weight);

        Map<Long, Double> userEvents = userEventWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Double oldWeight = userEvents.get(eventId);

        if (oldWeight != null && oldWeight >= weight) {
            log.debug("Weight not increased for userId={}, eventId={}, old={}, new={}",
                    userId, eventId, oldWeight, weight);
            return;
        }

        userEvents.put(eventId, weight);

        double oldTotal = eventTotalSums.getOrDefault(eventId, 0.0);
        double delta = (oldWeight == null) ? weight : (weight - oldWeight);
        eventTotalSums.put(eventId, oldTotal + delta);

        log.info("Updated total for event {}: {} -> {}", eventId, oldTotal, oldTotal + delta);

        for (Map.Entry<Long, Double> otherUserEvent : userEvents.entrySet()) {
            long otherEventId = otherUserEvent.getKey();
            if (otherEventId == eventId) continue;

            double otherWeight = otherUserEvent.getValue();
            double minWeight = Math.min(weight, otherWeight);

            long first = Math.min(eventId, otherEventId);
            long second = Math.max(eventId, otherEventId);

            Map<Long, Double> innerMap = minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>());
            innerMap.merge(second, minWeight, Double::sum);

            log.info("Updated min sum for pair ({}, {}): +{}", first, second, minWeight);

            sendSimilarity(first, second);
        }
    }

    private void sendSimilarity(long first, long second) {
        double sMin = minWeightsSums.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
        double sA = eventTotalSums.getOrDefault(first, 0.0);
        double sB = eventTotalSums.getOrDefault(second, 0.0);

        log.info("sendSimilarity: first={}, second={}, sMin={}, sA={}, sB={}",
                first, second, sMin, sA, sB);

        if (sA == 0 || sB == 0) {
            log.warn("sA or sB is 0, similarity will be 0");
            EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(0.0f)
                    .setTimestamp(Instant.now())
                    .build();
            kafkaTemplate.send(SIMILARITY_TOPIC, null, avro);
            log.info("Sent similarity with score 0 to topic: {}", SIMILARITY_TOPIC);
            return;
        }

        double norm1 = Math.sqrt(sA);
        double norm2 = Math.sqrt(sB);
        double similarity = sMin / (norm1 * norm2);
        similarity = Math.min(1.0, Math.max(0.0, similarity));

        log.info("Similarity: {} <-> {} = {}", first, second, similarity);

        EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore((float) similarity)
                .setTimestamp(Instant.now())
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
}