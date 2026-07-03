package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    @Query("SELECT ua.eventId, MAX(ua.weight) FROM UserAction ua WHERE ua.userId = :userId GROUP BY ua.eventId")
    List<Object[]> findMaxWeightsByUserId(@Param("userId") Long userId);

    List<UserAction> findByUserIdOrderByTimestampDesc(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT ua.userId, MAX(ua.weight) FROM UserAction ua WHERE ua.eventId = :eventId GROUP BY ua.userId")
    List<Object[]> findMaxWeightsByEventId(@Param("eventId") Long eventId);
}