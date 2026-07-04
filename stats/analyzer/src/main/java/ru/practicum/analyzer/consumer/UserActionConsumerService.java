package ru.practicum.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumerService {

    private final UserActionRepository userActionRepository;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "analyzer-group"
    )
    @Transactional
    public void consume(UserActionAvro action, Acknowledgment acknowledgment) {
        log.info("Received user action: userId={}, eventId={}, action={}, timestamp={}",
                action.getUserId(), action.getEventId(), action.getActionType(), action.getTimestamp());

        long timestampMillis = action.getTimestamp();
        LocalDateTime timestamp = Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        double weight = getWeight(action.getActionType());

        UserAction entity = UserAction.builder()
                .userId(action.getUserId())
                .eventId(action.getEventId())
                .actionType(action.getActionType().toString())
                .weight((weight * 10))
                .timestamp(timestamp)
                .build();

        userActionRepository.save(entity);
        log.info("Saved user action: {}", entity);

        acknowledgment.acknowledge();
    }

    private double getWeight(ActionTypeAvro actionType) {
        switch (actionType) {
            case VIEW: return 0.4;
            case REGISTER: return 0.8;
            case LIKE: return 1.0;
            default: return 0.0;
        }
    }
}