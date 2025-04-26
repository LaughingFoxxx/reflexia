package com.project.me.central_java_service.controller.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.me.central_java_service.service.UserAndDocumentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {
    private final ObjectMapper objectMapper;
    private final UserAndDocumentsService userAndDocumentsService;

    @Autowired
    public KafkaConsumer(ObjectMapper objectMapper, UserAndDocumentsService userAndDocumentsService) {
        this.objectMapper = objectMapper;
        this.userAndDocumentsService = userAndDocumentsService;
    }

    @KafkaListener(topics = "new-user", groupId = "core-response")
    public void newUserListener(String request) {
        try {
            JsonNode jsonNode = objectMapper.readTree(request);
            if (jsonNode != null) {
                log.info("KafkaController. Получен пользователь из топика \"new-user\". email={}", jsonNode.get("email").asText());
                String userEmail = jsonNode.get("email").asText();
                userAndDocumentsService.createUser(userEmail);
            }
        } catch (JsonProcessingException exception) {
            log.error("KafkaController. Ошибка при обработке пользователя: {}", exception.getMessage());
        }

    }
}
