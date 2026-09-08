package com.analytics.engine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    public static final String ACTIVITY_EVENTS_TOPIC = "user-activity-events";

    @Bean
    public NewTopic activityEventsTopic() {
        return TopicBuilder.name(ACTIVITY_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
