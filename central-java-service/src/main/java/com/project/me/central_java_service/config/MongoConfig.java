package com.project.me.central_java_service.config;

import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {
    @Value("${servers.mongoaddr}")
    private String mongoAddr;

    @Value("${servers.mongodbname}")
    private String mongoDbName;

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(MongoClients.create(mongoAddr), mongoDbName);
    }
}
