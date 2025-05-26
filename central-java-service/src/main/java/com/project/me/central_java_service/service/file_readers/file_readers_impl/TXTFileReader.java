package com.project.me.central_java_service.service.file_readers.file_readers_impl;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_readers.FileReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TXTFileReader implements FileReader {
    @Override
    public String readFile(MultipartFile file) {
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            return text.replace("\n", "<br>");
        } catch (IOException e) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения TXT файла");
        }
    }
}
