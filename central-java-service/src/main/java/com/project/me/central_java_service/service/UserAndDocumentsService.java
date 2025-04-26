package com.project.me.central_java_service.service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.project.me.central_java_service.model.dto.SaveDocumentDTO;
import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.model.entity.User;
import com.project.me.central_java_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserAndDocumentsService {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserAndDocumentsService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // Создание нового пользователя
    public void createUser(String userEmail) {
        log.info("UserAndDocumentsService. Запрос от AuthService. Создание нового пользователя с email={}", userEmail);

        Optional<User> userOptional = userRepository.findUserByUserEmail(userEmail);

        if (userOptional.isEmpty() || !userOptional.isPresent()) {
            User user = new User();
            user.setUserEmail(userEmail);
            user.setDocuments(new ArrayList<>());
            userRepository.save(user);
        } else {
            log.info("UserAndDocumentsService. Пользователь уже существует. Продолжаем работу");
        }
    }

    // Создать новый документ
    public Document createDocument(String userEmail) {
        log.info("UserAndDocumentsService. Создание нового документа для пользователя с email={}", userEmail);

        User user = userRepository.findUserByUserEmail(userEmail)
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Пользователь не найден")
                );

        Document document = getNewDocument();

        user.getDocuments().add(document);

        userRepository.save(user);
        return document;
    }

    // Создать новый документ с текстом
    public Document createDocument(String userEmail, String text) {
        log.info("UserAndDocumentsService. Создание нового документа с текстом для пользователя с email={}", userEmail);

        User user = userRepository.findUserByUserEmail(userEmail)
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Пользователь не найден")
                );

        Document document = getNewDocument();
        document.setText(text);

        user.getDocuments().add(document);

        userRepository.save(user);
        return document;
    }

    // Получить список всех документов пользователя
    public List<Document> getAllDocuments(String userEmail) {
        log.info("UserAndDocumentsService. Получение всех документов для {}", userEmail);

        User user = userRepository.findUserByUserEmail(userEmail)
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Пользователь не найден")
                );

        return user.getDocuments();
    }

    // Сохранить/обновить документ
    public Boolean saveOrUpdateDocument(SaveDocumentDTO documentDTO, String userEmail) {
        log.info("UserAndDocumentsService. Сохранение изменений для документа с id={} пользователя {}",documentDTO.documentId(), userEmail);

        User user = userRepository.findUserByUserEmail(userEmail)
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Пользователь не найден")
                );

        Document document = user.getDocuments()
                .stream()
                .filter(x -> x.getDocumentId().equals(documentDTO.documentId()))
                .findFirst()
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.NOT_FOUND, "Документ не найден")
                );

        document.setUpdatedAt(LocalDateTime.now());
        document.setDocumentName(documentDTO.documentName());
        document.setText(documentDTO.text());

        userRepository.save(user);

        return true;
    }

    public boolean deleteOneDocument(String documentId, String userEmail) {
        log.info("UserAndDocumentsService. Удаление документа с documentId={} для пользователя с email={}", documentId, userEmail);

        Query query = new Query(Criteria.where("userEmail").is(userEmail));
        Update update = new Update().pull("documents", new BasicDBObject("documentId", documentId));

        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

        return result.getMatchedCount() > 0 && result.getModifiedCount() > 0;
    }

    public Document getNewDocument() {
        Document document = new Document();

        document.setDocumentId(UUID.randomUUID().toString());
        document.setDocumentName("Новый документ");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document.setText("");

        return document;
    }
}