package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.stats.service.dashboard.RecommendedEventProto;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository similarityRepository;

    private static final int DEFAULT_K = 10;

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.info("Getting recommendations for user {} (max {})", userId, maxResults);

        List<UserAction> recentActions = userActionRepository.findByUserIdOrderByTimestampDesc(userId);
        if (recentActions.isEmpty()) {
            log.info("User {} has no actions, returning empty", userId);
            return Collections.emptyList();
        }

        Set<Long> recentEventIds = recentActions.stream()
                .map(UserAction::getEventId)
                .limit(DEFAULT_K)
                .collect(Collectors.toSet());

        Set<Long> interactedEvents = userActionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(UserAction::getEventId).collect(Collectors.toSet());

        Map<Long, Double> candidateScores = new HashMap<>();

        for (Long eventId : recentEventIds) {
            List<EventSimilarity> similarities = similarityRepository.findSimilaritiesByEventId(eventId);
            for (EventSimilarity sim : similarities) {
                Long otherEvent = sim.getEventA().equals(eventId) ? sim.getEventB() : sim.getEventA();
                if (interactedEvents.contains(otherEvent)) continue;
                candidateScores.merge(otherEvent, sim.getScore(), Double::sum);
            }
        }

        List<RecommendedEventProto> result = candidateScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info("Returning {} recommendations for user {}", result.size(), userId);
        return result;
    }

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("Getting similar events for event {}, user {}, max {}", eventId, userId, maxResults);

        List<EventSimilarity> similarities = similarityRepository.findSimilaritiesByEventId(eventId);
        if (similarities.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> interacted = userActionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(UserAction::getEventId).collect(Collectors.toSet());

        List<RecommendedEventProto> result = similarities.stream()
                .map(sim -> {
                    Long other = sim.getEventA().equals(eventId) ? sim.getEventB() : sim.getEventA();
                    return Map.entry(other, sim.getScore());
                })
                .filter(e -> !interacted.contains(e.getKey()))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info("Returning {} similar events", result.size());
        return result;
    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.info("Getting interactions count for events: {}", eventIds);

        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> resultMap = new HashMap<>();
        for (Long eventId : eventIds) {
            List<Object[]> maxWeights = userActionRepository.findMaxWeightsByUserId(eventId);
            Double sum = userActionRepository.findAll().stream()
                    .filter(ua -> ua.getEventId().equals(eventId))
                    .collect(Collectors.groupingBy(UserAction::getUserId,
                            Collectors.summingInt(UserAction::getWeight)))
                    .values().stream().mapToDouble(Integer::doubleValue).sum();
            resultMap.put(eventId, sum);
        }

        List<RecommendedEventProto> result = resultMap.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return result;
    }
}