package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionConsumerService {

    private final UserActionRepository userActionRepository;

    @KafkaListener(topics = "stats.user-actions.v1", groupId = "analyzer-group")
    @Transactional
    public void consume(UserActionAvro action) {
        log.info("Received user action: userId={}, eventId={}, action={}, timestamp={}",
                action.getUserId(), action.getEventId(), action.getActionType(), action.getTimestamp());

        long timestampMillis = action.getTimestamp();

        UserAction entity = UserAction.builder()
                .userId(action.getUserId())
                .eventId(action.getEventId())
                .actionType(action.getActionType().toString())
                .weight(getWeight(action.getActionType()))
                .timestamp(Instant.ofEpochMilli(timestampMillis).atZone(ZoneOffset.UTC).toLocalDateTime())
                .build();

        userActionRepository.save(entity);
        log.info("Saved user action: {}", entity);
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