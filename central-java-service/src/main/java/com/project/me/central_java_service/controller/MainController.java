package com.project.me.central_java_service.controller;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.model.dto.*;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.model.entity.TextAiRequest;
import com.project.me.central_java_service.model.entity.User;
import com.project.me.central_java_service.repository.UserRepository;
import com.project.me.central_java_service.service.*;
import com.project.me.central_java_service.util.PreMadePrompts;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/text")
public class MainController {
    private final CoreService coreService;
    private final UserDocumentsService userDocumentsService;
    private final ExportFileService exportFileService;
    private final ReaderFileService readerFileService;
    private final UserRepository userRepository;
    private final PreMadePrompts preMadePrompts;
    private final VirusScannerService virusScannerService;

    @Autowired
    public MainController(CoreService coreService,
                          UserDocumentsService userDocumentsService,
                          ExportFileService exportFileService,
                          ReaderFileService readerFileService,
                          UserRepository userRepository, PreMadePrompts preMadePrompts, VirusScannerService virusScannerService) {
        this.coreService = coreService;
        this.userDocumentsService = userDocumentsService;
        this.readerFileService = readerFileService;
        this.exportFileService = exportFileService;
        this.userRepository = userRepository;
        this.preMadePrompts = preMadePrompts;
        this.virusScannerService = virusScannerService;
    }

    // Запрос на обработку текста и занесение его в базу
    @Async(value = "taskExecutor")
    @PostMapping("/process-text")
    public CompletableFuture<ResponseEntity<TextResponseDTO>> processText(
            @RequestBody @Valid TextRequestDTO textRequestDTO,
            @RequestHeader(value = "From") String userEmail) {
        log.info("MainController. POST-запрос. text={}, instruction={}, email={}", textRequestDTO.text().substring(0, Math.min(20, textRequestDTO.text().length())), textRequestDTO.instruction(), userEmail);
        return coreService.processText(textRequestDTO)
                .orTimeout(60, TimeUnit.SECONDS)
                .thenApply(result -> {
                    TextAiRequest request = new TextAiRequest();
                    request.setAiRequestId(UUID.randomUUID().toString());

                    Map<String, String> prompts = preMadePrompts.getOptions();
                    if (prompts.containsKey(textRequestDTO.instruction())) {
                        request.setPrompt("Опция: " + prompts.get(textRequestDTO.instruction().toLowerCase()));
                    } else {
                        request.setPrompt(textRequestDTO.instruction());
                    }

                    request.setRequestText(textRequestDTO.text());
                    request.setResponseText(result.result());
                    request.setRequestTime(LocalDateTime.now());

                    User user = userRepository.findUserByUserEmail(userEmail)
                            .orElseThrow(
                                    () -> new BaseCoreServiceException(HttpStatus.NOT_FOUND, "Пользователь не найден")
                            );

                    user.getRequests().add(request);
                    userRepository.save(user);

                    return ResponseEntity.ok().body(result);
                })
                .exceptionally(throwable -> {
                    System.err.println("Ошибка: " + throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    // Запрос на считывание файла .doc или .docx
    @PostMapping(value = "/upload-document-file", produces = "application/json")
    public ResponseEntity<?> uploadMicrosoftFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("From") String userEmail)
    {
        log.info("MainController. POST-запрос. Считывание файла: {}", file.getOriginalFilename());
        if (!virusScannerService.scanFile(file)) { return ResponseEntity.badRequest().body("Файл заражён вирусом"); }
        Document document = readerFileService.readFile(userEmail, file.getOriginalFilename(), file);
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    // Запрос на скачивание файла
    @PostMapping("/export-document-file")
    public ResponseEntity<Resource> exportFile(
            @RequestHeader("From") String userEmail,
            @RequestBody @Valid DocumentToExportDTO exportDTO
    ) {
        log.info("MainController. POST-запрос. Запрос на экспорт файла с id={} для пользователя с email={}", exportDTO.documentId(), userEmail);
        File file = exportFileService.exportFile(exportDTO, userEmail);

        Resource resource;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(Path.of(file.getAbsolutePath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().body(resource);
    }

    // Запрос на создание нового пользователя
    @PostMapping("/create-new-user")
    public ResponseEntity<HttpStatus> createNewUser(@RequestParam String userEmail) {
        log.info("MainController. POST-запрос от Auth-сервиса. Создание нового пользователя на Core-сервисе. Email={}", userEmail);
        userDocumentsService.createUser(userEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Запрос на получение всех документов для пользователя
    @GetMapping("/all-user-documents")
    public ResponseEntity<List<Document>> getAllUserSessions(@RequestHeader(value = "From") String userEmail) {
        log.info("MainController. GET-запрос. Получение списка всех документов для {}", userEmail);
        return new ResponseEntity<>(userDocumentsService.getAllDocuments(userEmail), HttpStatus.OK);
    }

    // Запрос на создание нового документа
    @PostMapping("/create-new-document")
    public ResponseEntity<Document> createNewDocument(
            @RequestHeader(value = "From") String userEmail
    ) {
        log.info("MainController. POST-запрос. Создание нового документа для пользователя email={}", userEmail);
        return new ResponseEntity<>(userDocumentsService.createDocument(userEmail), HttpStatus.OK);
    }

    // Запрос на сохранение измнений в документе
    @PutMapping(value = "/save-document-changes", produces = "application/json")
    public ResponseEntity<HttpStatus> saveDocumentChanges(@RequestBody @Valid SaveDocumentDTO documentDTO, @RequestHeader("From") String userEmail) {
        log.info("MainController. PUT-запрос. Внесение изменений в документ с documentId={} для пользователя с email={}", documentDTO.documentId(), userEmail);
        if (userDocumentsService.saveOrUpdateDocument(documentDTO, userEmail)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Запрос на смену имени у документа
    @PutMapping(value = "/update-document-name")
    public ResponseEntity<?> updateDocumentName(@RequestBody @Valid UpdateDocumentNameDTO documentNameDTO, @RequestHeader("From") String userEmail) {
        log.info("MainController. PUT-запрос. Запрос на переименование документа. documentId={}", documentNameDTO.documentId());
        if (userDocumentsService.updateDocumentName(documentNameDTO, userEmail)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Запрос на удаление документа
    @DeleteMapping("/delete-document")
    public ResponseEntity<HttpStatus> deleteDocument(@RequestParam String documentId, @RequestHeader("From") String userEmail) {
        log.info("MainController. DELETE-запрос. Удаление одного документа по documentId={} для пользователя с email={}", documentId, userEmail);
        if (userDocumentsService.deleteOneDocument(documentId, userEmail)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Запрос на получение имейла пользователя
    @GetMapping("/get-user-email")
    public ResponseEntity<Map<String, String>> getUserEmail(@RequestHeader("From") String userEmail) {
        log.info("MainController. GET-запрос. Получение email пользователя с email={}", userEmail);
        return ResponseEntity.ok().body(Map.of("email", userEmail));
    }

    // Запрос на получение истории запросов
    @GetMapping("/get-request-history")
    public ResponseEntity<List<TextAiRequest>> getRequestHistory(@RequestHeader("From") String userEmail) {
        log.info("MainController. GET-запрос. Получение истории запросов для пользователя с email={}", userEmail);
        List<TextAiRequest> res = userDocumentsService.getUserRequestHistory(userEmail);
        return ResponseEntity.ok().body(res);
    }
}