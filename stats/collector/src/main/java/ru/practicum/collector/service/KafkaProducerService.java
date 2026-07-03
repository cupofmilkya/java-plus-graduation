package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.service.collector.ActionTypeProto;
import ru.practicum.stats.service.collector.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final String TOPIC = "stats.user-actions.v1";

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    public void sendAction(UserActionProto proto) {
        long timestampMillis = proto.getTimestamp().getSeconds() * 1000 +
                proto.getTimestamp().getNanos() / 1_000_000;

        UserActionAvro avro = UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(convertActionType(proto.getActionType()))
                .setTimestamp(timestampMillis)  // Теперь передаем long
                .build();

        log.info("Sending user action to Kafka: userId={}, eventId={}, action={}, timestamp={}",
                avro.getUserId(), avro.getEventId(), avro.getActionType(), avro.getTimestamp());

        kafkaTemplate.send(TOPIC, String.valueOf(proto.getUserId()), avro);
    }

    private ActionTypeAvro convertActionType(ActionTypeProto proto) {
        switch (proto) {
            case ACTION_VIEW:
                return ActionTypeAvro.VIEW;
            case ACTION_REGISTER:
                return ActionTypeAvro.REGISTER;
            case ACTION_LIKE:
                return ActionTypeAvro.LIKE;
            default:
                throw new IllegalArgumentException("Unknown action type: " + proto);
        }
    }
}