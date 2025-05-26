package com.project.me.central_java_service.service.file_readers;

import com.project.me.central_java_service.service.file_readers.file_readers_impl.DocFileReader;
import com.project.me.central_java_service.service.file_readers.file_readers_impl.DocXFileReader;
import com.project.me.central_java_service.service.file_readers.file_readers_impl.PDFFileReader;
import com.project.me.central_java_service.service.file_readers.file_readers_impl.TXTFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class FileReaderFactory {
    private final ApplicationContext context;

    @Autowired
    public FileReaderFactory(ApplicationContext context) {
        this.context = context;
    }

    public FileReader getFileReader(String fileName) {
        String extension = getFileExtension(fileName);
        return switch (extension) {
            case "doc" -> context.getBean(DocFileReader.class);
            case "docx" -> context.getBean(DocXFileReader.class);
            case "pdf" -> context.getBean(PDFFileReader.class);
            case "txt" -> context.getBean(TXTFileReader.class);
            default -> throw new IllegalArgumentException("Неподдержимаемый формат: " + extension);
        };
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}

