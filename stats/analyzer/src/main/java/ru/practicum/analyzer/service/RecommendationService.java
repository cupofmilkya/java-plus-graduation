package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserActionRepository;
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

        List<UserAction> allUserActions = userActionRepository.findByUserIdOrderByTimestampDesc(userId);
        if (allUserActions.isEmpty()) {
            log.info("User {} has no actions, returning empty", userId);
            return Collections.emptyList();
        }

        Set<Long> recentEventIds = allUserActions.stream()
                .map(UserAction::getEventId)
                .limit(DEFAULT_K)
                .collect(Collectors.toSet());

        Set<Long> interactedEvents = allUserActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> candidateScores = new HashMap<>();

        for (Long eventId : recentEventIds) {
            List<EventSimilarity> similarities = similarityRepository.findSimilaritiesByEventId(eventId);
            for (EventSimilarity sim : similarities) {
                Long otherEvent = sim.getEventA().equals(eventId) ? sim.getEventB() : sim.getEventA();
                if (!interactedEvents.contains(otherEvent)) {
                    candidateScores.merge(otherEvent, sim.getScore(), Double::sum);
                }
            }
        }

        return candidateScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("Getting similar events for event {}, user {}, max {}", eventId, userId, maxResults);

        List<EventSimilarity> similarities = similarityRepository.findSimilaritiesByEventId(eventId);
        if (similarities.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> interacted = userActionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        return similarities.stream()
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
    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.info("Getting interactions count for events: {}", eventIds);

        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecommendedEventProto> result = new ArrayList<>();

        for (Long eventId : eventIds) {
            List<Object[]> maxWeightsByUser = userActionRepository.findMaxWeightsByEventId(eventId);

            double totalScore = 0.0;
            for (Object[] row : maxWeightsByUser) {
                Integer maxWeight = (Integer) row[1];
                totalScore += maxWeight != null ? maxWeight : 0;
            }

            result.add(RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(totalScore)
                    .build());
        }

        log.info("Returning interactions count for {} events", result.size());
        return result;
    }
}