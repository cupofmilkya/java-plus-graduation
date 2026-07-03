package ru.practicum.service;

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

    private static final int VIEW_WEIGHT = 1;
    private static final int REGISTER_WEIGHT = 2;
    private static final int LIKE_WEIGHT = 3;

    private final Map<Long, Map<Long, Integer>> userEventWeights = new ConcurrentHashMap<>();
    private final Map<Long, Double> eventTotalSums = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public SimilarityCalculator(KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        int weight = getWeight(action.getActionType());

        Map<Long, Integer> userEvents = userEventWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Integer oldWeight = userEvents.get(eventId);

        if (oldWeight != null && oldWeight >= weight) {
            log.debug("Weight for user {} event {} not increased (old={}, new={})", userId, eventId, oldWeight, weight);
            return;
        }

        userEvents.put(eventId, weight);

        double oldTotal = eventTotalSums.getOrDefault(eventId, 0.0);
        double delta = (oldWeight == null) ? weight : (weight - oldWeight);
        eventTotalSums.put(eventId, oldTotal + delta);

        for (Map.Entry<Long, Integer> otherEntry : userEvents.entrySet()) {
            long otherEventId = otherEntry.getKey();
            if (otherEventId == eventId) continue;
            int otherWeight = otherEntry.getValue();
            int minWeight = Math.min(weight, otherWeight);
            addToMinSum(eventId, otherEventId, minWeight);
        }

        Map<Long, Double> pairsFirst = minWeightsSums.getOrDefault(eventId, new ConcurrentHashMap<>());
        for (Map.Entry<Long, Double> pair : pairsFirst.entrySet()) {
            long second = pair.getKey();
            sendSimilarity(eventId, second, pair.getValue());
        }

        for (Map.Entry<Long, Map<Long, Double>> entry : minWeightsSums.entrySet()) {
            long first = entry.getKey();
            if (first == eventId) continue;
            Double sMin = entry.getValue().get(eventId);
            if (sMin != null) {
                sendSimilarity(first, eventId, sMin);
            }
        }
    }

    private void sendSimilarity(long eventA, long eventB, double sMin) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        double sA = eventTotalSums.getOrDefault(first, 0.0);
        double sB = eventTotalSums.getOrDefault(second, 0.0);
        if (sA == 0 || sB == 0) return;
        double similarity = sMin / (Math.sqrt(sA) * Math.sqrt(sB));
        similarity = Math.min(1.0, Math.max(0.0, similarity));

        EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore((float) similarity)
                .setTimestamp(Instant.now())
                .build();

        kafkaTemplate.send(SIMILARITY_TOPIC, String.valueOf(first), avro);
        log.debug("Sent similarity: {} <-> {} = {}", first, second, similarity);
    }

    private void addToMinSum(long eventA, long eventB, int minWeight) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        Map<Long, Double> innerMap = minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>());
        innerMap.merge(second, (double) minWeight, Double::sum);
    }

    private int getWeight(ActionTypeAvro actionType) {
        switch (actionType) {
            case VIEW: return VIEW_WEIGHT;
            case REGISTER: return REGISTER_WEIGHT;
            case LIKE: return LIKE_WEIGHT;
            default: return 0;
        }
    }
}