package com.project.me.central_java_service.controller;

import com.project.me.central_java_service.dto.SaveDocumentDTO;
import com.project.me.central_java_service.dto.TextRequestDTO;
import com.project.me.central_java_service.dto.TextResponseDTO;
import com.project.me.central_java_service.model.Document;
import com.project.me.central_java_service.service.CoreService;
import com.project.me.central_java_service.service.UserAndDocumentsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/text")
public class MainController {

    private final CoreService coreService;
    private final UserAndDocumentsService userAndDocumentsService;

    @Autowired
    public MainController(CoreService coreService,
                          UserAndDocumentsService userAndDocumentsService
    ) {
        this.coreService = coreService;
        this.userAndDocumentsService = userAndDocumentsService;
    }

    // Запрос на обработку текста и занесение его в базу
    @Async(value = "taskExecutor")
    @PostMapping("/process-text")
    public CompletableFuture<ResponseEntity<TextResponseDTO>> processText(
            @RequestBody TextRequestDTO textRequestDTO,
            @RequestHeader(value = "From") String emailHeader
    ) {
        log.info("MainController. POST-запрос. text={}, instruction={}", textRequestDTO.text().substring(0, Math.min(20, textRequestDTO.text().length())), textRequestDTO.instruction());
        if (emailHeader == null || emailHeader.trim().isEmpty()) {
            throw new RuntimeException("Пустой заголовок");
        }

        return coreService.processText(textRequestDTO)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                );
    }

    // Запрос на создание нового пользователя
    @PostMapping("/create-new-user")
    public ResponseEntity<HttpStatus> createNewUser(@RequestParam String userEmail) {
        log.info("MainController. POST-запрос от Auth-сервиса. Создание нового пользователя на Core-сервисе. Email={}", userEmail);

        userAndDocumentsService.createUser(userEmail);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Запрос на получение всех документов для пользователя
    @GetMapping("/all-user-documents")
    public ResponseEntity<List<Document>> getAllUserSessions(@RequestHeader(value = "From") String userEmail) {
        log.info("MainController. GET-запрос. Получение списка всех документов для {}", userEmail);

        return new ResponseEntity<>(userAndDocumentsService.getAllDocuments(userEmail), HttpStatus.OK);
    }

    // Запрос на создание нового документа
    @PostMapping("/create-new-document")
    public ResponseEntity<Document> createNewDocument(@RequestHeader(value = "From") String userEmail) {
        log.info("MainController. POST-запрос. Создание нового документа для пользователя email={}", userEmail);

        return new ResponseEntity<>(userAndDocumentsService.createDocument(userEmail), HttpStatus.OK);
    }

    // Запрос на сохранение измнений в документе
    @PutMapping("/save-document-changes")
    public ResponseEntity<HttpStatus> saveDocumentChanges(@RequestBody @Valid SaveDocumentDTO documentDTO, @RequestHeader("From") String userEmail) {
        log.info("MainController. PUT-запрос. Внесение изменений в документ с documentId={} для пользователя с email={}", documentDTO.documentId(), userEmail);

        if (userAndDocumentsService.saveOrUpdateDocument(documentDTO, userEmail)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Запрос на удаление документа
    @DeleteMapping("/delete-document")
    public ResponseEntity<HttpStatus> deleteDocument(@RequestParam String documentId, @RequestParam String userEmail) {
        log.info("MainController. DELETE-запрос. Удаление одного документа по documentId={} для пользователя с email={}", documentId, userEmail);

        if (userAndDocumentsService.deleteOneDocument(documentId, userEmail)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}