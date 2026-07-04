package ru.practicum.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final EventSimilarityRepository similarityRepository;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            groupId = "analyzer-group",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(EventSimilarityAvro avro) {
        log.info("Received similarity: eventA={}, eventB={}, score={}, timestamp={}",
                avro.getEventA(), avro.getEventB(), avro.getScore(), avro.getTimestamp());

        Instant timestampMillis = avro.getTimestamp();
        LocalDateTime timestamp = Instant.ofEpochMilli(timestampMillis.toEpochMilli())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        EventSimilarity entity = EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score((double) avro.getScore())
                .timestamp(timestamp)
                .build();

        similarityRepository.findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                .ifPresentOrElse(
                        existing -> {
                            existing.setScore((double) avro.getScore());
                            existing.setTimestamp(timestamp);
                            similarityRepository.save(existing);
                            log.info("Updated similarity: {} <-> {} = {}",
                                    existing.getEventA(), existing.getEventB(), existing.getScore());
                        },
                        () -> {
                            similarityRepository.save(entity);
                            log.info("Saved new similarity: {} <-> {} = {}",
                                    entity.getEventA(), entity.getEventB(), entity.getScore());
                        }
                );
    }
}