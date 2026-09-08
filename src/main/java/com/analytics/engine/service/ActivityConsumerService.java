package com.analytics.engine.service;

import com.analytics.engine.config.KafkaConfig;
import com.analytics.engine.dto.ActivityEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer service.
 * Consumes user activity logs asynchronously from Kafka topic,
 * decoupling ingestion from database persistence.
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class ActivityConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ActivityConsumerService.class);

    private final ActivityPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    public ActivityConsumerService(ActivityPersistenceService persistenceService, ObjectMapper objectMapper) {
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaConfig.ACTIVITY_EVENTS_TOPIC, groupId = "analytics-consumer-group")
    public void consumeActivityEvent(String message) {
        try {
            log.info("[Kafka Consumer] Received message from Kafka: {}", message);
            ActivityEventDto eventDto = objectMapper.readValue(message, ActivityEventDto.class);
            persistenceService.saveEvent(eventDto);
        } catch (Exception e) {
            log.error("[Kafka Consumer Error] Failed to process message: {}", e.getMessage(), e);
        }
    }
}
