package com.project.me.central_java_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.me.central_java_service.model.dto.TextRequestDTO;
import com.project.me.central_java_service.model.dto.TextResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CoreService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CompletableFuture<TextResponseDTO>> messagesBuffer = new ConcurrentHashMap<>();

    @Autowired
    public CoreService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public CompletableFuture<TextResponseDTO> processText(TextRequestDTO requestDTO) {
        String requestId = UUID.randomUUID().toString();
        log.info("CoreService. Запрос на обработку текста. Буффер: {}", requestId);
        CompletableFuture<TextResponseDTO> future = new CompletableFuture<>();
        messagesBuffer.put(requestId, future);

        try {
            String message = buildJSON(requestId, requestDTO.instruction(), requestDTO.text());
            kafkaTemplate.send("text-processing-requests", message)
                    .whenComplete(
                            (request, ex) -> {
                                if (ex != null) {
                                    messagesBuffer.remove(requestId);
                                    future.completeExceptionally(ex);
                                }
                            });
        } catch (JsonProcessingException e) {
            messagesBuffer.remove(requestId);
            future.completeExceptionally(e);
        }

        return future;
    }

    @KafkaListener(topics = "text-processing-response", groupId = "core-response")
    public void listener(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String requestId = jsonNode.get("requestId").asText();
            log.info("CoreService. Результат обработки получен из Кафки. Буффер: {}", requestId);
            String processedText = jsonNode.get("processedText").asText();
            CompletableFuture<TextResponseDTO> future = messagesBuffer.remove(requestId);
            if (future != null) {
                future.complete(new TextResponseDTO(processedText));
            }
        } catch (JsonProcessingException e) {
            System.out.println("Ошибка при обработке результата: " + e.getMessage());
        }
    }

    private String buildJSON(String requestId, String instruction, String text) throws JsonProcessingException {
        Map<String, String> message = new HashMap<>();
        message.put("requestId", requestId);
        message.put("instruction", instruction);
        message.put("text", text);
        return objectMapper.writeValueAsString(message);
    }
}
