package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityConsumerService {

    private final EventSimilarityRepository similarityRepository;

    @KafkaListener(topics = "stats.events-similarity.v1", groupId = "analyzer-group")
    @Transactional
    public void consume(EventSimilarityAvro avro) {
        log.info("Received similarity: eventA={}, eventB={}, score={}, timestamp={}",
                avro.getEventA(), avro.getEventB(), avro.getScore(), avro.getTimestamp());

        EventSimilarity entity = EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score(avro.getScore())
                .timestamp(Instant.ofEpochMilli(avro.getTimestamp().toEpochMilli()).atZone(ZoneOffset.UTC).toLocalDateTime())
                .build();

        similarityRepository.findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                .ifPresentOrElse(
                        existing -> {
                            existing.setScore(avro.getScore());
                            existing.setTimestamp(entity.getTimestamp());
                            similarityRepository.save(existing);
                        },
                        () -> similarityRepository.save(entity)
                );
        log.info("Saved/updated similarity: {} <-> {} = {}", entity.getEventA(), entity.getEventB(), entity.getScore());
    }
}