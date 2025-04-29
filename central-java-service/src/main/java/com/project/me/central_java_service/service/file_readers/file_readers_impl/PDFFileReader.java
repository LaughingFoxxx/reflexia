package com.project.me.central_java_service.service.file_readers.file_readers_impl;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_readers.FileReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
@Scope("prototype")
public class PDFFileReader implements FileReader {
    @Override
    public String readFile(MultipartFile file) {
        StringBuilder htmlContent = new StringBuilder("<html><body>");
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            htmlContent.append(text.replace("\t", "&emsp;").replace("\n", "<br>"));
        } catch (IOException e) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения pdf файла");
        }
        return htmlContent.toString();
    }
}
