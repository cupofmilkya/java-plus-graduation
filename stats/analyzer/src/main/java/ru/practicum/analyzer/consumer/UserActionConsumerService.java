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

        UserAction entity = UserAction.builder()
                .userId(action.getUserId())
                .eventId(action.getEventId())
                .actionType(action.getActionType().toString())
                .weight(getWeight(action.getActionType()))
                .timestamp(timestamp)
                .build();

        userActionRepository.save(entity);
        log.info("Saved user action: {}", entity);

        acknowledgment.acknowledge();
    }

    private int getWeight(ActionTypeAvro actionType) {
        switch (actionType) {
            case VIEW: return 1;
            case REGISTER: return 2;
            case LIKE: return 3;
            default: return 0;
        }
    }
}