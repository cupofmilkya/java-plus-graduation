package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("SELECT es FROM EventSimilarity es WHERE es.eventA = :eventId OR es.eventB = :eventId ORDER BY es.score DESC")
    List<EventSimilarity> findSimilaritiesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT es FROM EventSimilarity es WHERE (es.eventA = :eventId OR es.eventB = :eventId) AND es.score >= :minScore ORDER BY es.score DESC")
    List<EventSimilarity> findSimilaritiesByEventIdWithMinScore(@Param("eventId") Long eventId,
                                                                @Param("minScore") Double minScore);
}