package com.project.me.central_java_service.controller;

import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.dto.SaveDocumentDTO;
import com.project.me.central_java_service.model.dto.TextRequestDTO;
import com.project.me.central_java_service.model.dto.TextResponseDTO;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.service.CoreService;
import com.project.me.central_java_service.service.ExportFileService;
import com.project.me.central_java_service.service.UserDocumentsService;
import com.project.me.central_java_service.service.file_readers.FileReader;
import com.project.me.central_java_service.service.file_readers.FileReaderFactory;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/text")
public class MainController {
    private final CoreService coreService;
    private final UserDocumentsService userDocumentsService;
    private final FileReaderFactory fileReaderFactory;
    private final ExportFileService exportFileService;

    @Autowired
    public MainController(CoreService coreService,
                          UserDocumentsService userDocumentsService,
                          FileReaderFactory fileReaderFactory,
                          ExportFileService exportFileService
    ) {
        this.coreService = coreService;
        this.userDocumentsService = userDocumentsService;
        this.fileReaderFactory = fileReaderFactory;
        this.exportFileService = exportFileService;
    }

    // Запрос на обработку текста и занесение его в базу
    @Async(value = "taskExecutor")
    @PostMapping("/process-text")
    public CompletableFuture<ResponseEntity<TextResponseDTO>> processText(
            @RequestBody @Valid TextRequestDTO textRequestDTO,
            @RequestHeader(value = "From") String emailHeader) {

        log.info("MainController. POST-запрос. text={}, instruction={}", textRequestDTO.text().substring(0, Math.min(20, textRequestDTO.text().length())), textRequestDTO.instruction());
        return coreService.processText(textRequestDTO)
                .orTimeout(60, TimeUnit.SECONDS)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                );
    }

    // Запрос на считывание файла .doc или .docx
    @PostMapping(value = "/upload-document-file", produces = "application/json")
    public ResponseEntity<Document> uploadMicrosoftFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("From") String userEmail)
    {
        log.info("MainController. POST-запрос. Считывание файла: {}", file.getOriginalFilename());
        FileReader fileReader = fileReaderFactory.getFileReader(file.getOriginalFilename());
        return new ResponseEntity<>(userDocumentsService.createDocument(userEmail, fileReader.readFile(file)), HttpStatus.OK);
    }

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
}