package com.project.me.central_java_service.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    public NewTopic createRequestTextProcessTopic() {
        return TopicBuilder.name("text-processing-requests")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic createResponseTextProcessTopic() {
        return TopicBuilder.name("text-processing-response")
                .partitions(1)
                .replicas(1)
                .build();
    }
}