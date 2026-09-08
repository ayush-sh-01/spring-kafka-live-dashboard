package com.analytics.engine.service;

import com.analytics.engine.config.KafkaConfig;
import com.analytics.engine.dto.ActivityEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * High-Throughput Event Ingestion Producer.
 * Ingests user activity clicks and streams them to Kafka topic.
 * Features an automatic graceful in-memory fallback if Kafka broker is offline.
 */
@Service
public class ActivityProducerService {

    private static final Logger log = LoggerFactory.getLogger(ActivityProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ActivityPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    public ActivityProducerService(@org.springframework.beans.factory.annotation.Autowired(required = false) 
                                   KafkaTemplate<String, String> kafkaTemplate,
                                   ActivityPersistenceService persistenceService,
                                   ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
    }

    public void publishEvent(ActivityEventDto eventDto) {
        if (kafkaTemplate != null) {
            try {
                String jsonPayload = objectMapper.writeValueAsString(eventDto);
                // Attempt to send to Kafka topic with short timeout to prevent blocking HTTP threads
                kafkaTemplate.send(KafkaConfig.ACTIVITY_EVENTS_TOPIC, eventDto.getUsername(), jsonPayload)
                        .get(1000, TimeUnit.MILLISECONDS);
                log.info("[Kafka Producer] Successfully queued activity event for user: {}", eventDto.getUsername());
                return;
            } catch (Exception ex) {
                log.warn("[Kafka Broker Offline/Unavailable] Gracefully falling back to direct persistence: {}", ex.getMessage());
            }
        }
        // Fallback or dev mode: direct persistence
        persistenceService.saveEvent(eventDto);
    }
}
