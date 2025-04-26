package com.project.me.central_java_service.service;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class TikaFilesReader {
    public String readFile(MultipartFile file) {
        log.info("TikaFilesReader. Считывание файла: {}", file.getOriginalFilename());

        ToXMLContentHandler handler = new ToXMLContentHandler();
        Parser parser = new AutoDetectParser();

        try (InputStream inputStream = file.getInputStream()) {
            parser.parse(inputStream, handler, new Metadata(), new ParseContext());
        } catch (TikaException | IOException | SAXException e) {
            log.error("TikaFilesReader. Ошибка считывания файла");
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения файла");
        }
        return handler.toString();
    }
}
