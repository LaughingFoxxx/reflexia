package com.project.me.central_java_service.service;

import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.service.file_readers.FileReader;
import com.project.me.central_java_service.service.file_readers.FileReaderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ReaderFileService {
    private final FileReaderFactory fileReaderFactory;
    private final UserDocumentsService userDocumentsService;

    @Autowired
    public ReaderFileService(FileReaderFactory fileReaderFactory, UserDocumentsService userDocumentsService) {
        this.fileReaderFactory = fileReaderFactory;
        this.userDocumentsService = userDocumentsService;
    }

    public Document readFile(String userEmail, String fileName, MultipartFile file) {
        log.info("ReaderFileService. Начато считывание файла");
        FileReader fileReader = fileReaderFactory.getFileReader(fileName);
        String fileContent = fileReader.readFile(file);
        return userDocumentsService.createDocument(userEmail, fileContent);
    }
}
