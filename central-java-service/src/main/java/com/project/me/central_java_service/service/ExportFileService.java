package com.project.me.central_java_service.service;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.model.entity.User;
import com.project.me.central_java_service.repository.UserRepository;
import com.project.me.central_java_service.service.file_exporters.FileExporter;
import com.project.me.central_java_service.service.file_exporters.FileExporterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class ExportFileService {
    private final UserRepository userRepository;
    private final FileExporterFactory exporterFactory;

    @Autowired
    public ExportFileService(UserRepository userRepository, FileExporterFactory exporterFactory) {
        this.userRepository = userRepository;
        this.exporterFactory = exporterFactory;
    }

    public File exportFile(DocumentToExportDTO exportDTO, String userEmail) {
        User user = userRepository.findUserByUserEmail(userEmail).orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.UNAUTHORIZED, "Пользователь не найден")
        );

        Document document = user.getDocuments().stream()
                .filter(x -> x.getDocumentId().equals(exportDTO.documentId()))
                .findFirst()
                .orElseThrow(
                        () -> new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Документ не существует")
                );

        FileExporter fileExporter = exporterFactory.getFileExporter(exportDTO.format());

        return fileExporter.exportFile(exportDTO, document);
    }
}
