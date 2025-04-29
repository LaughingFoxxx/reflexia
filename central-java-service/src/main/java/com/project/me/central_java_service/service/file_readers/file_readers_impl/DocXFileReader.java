package com.project.me.central_java_service.service.file_readers.file_readers_impl;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_readers.FileReader;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
@Scope("prototype")
public class DocXFileReader implements FileReader {
    @Override
    public String readFile(MultipartFile file) {
        StringBuilder htmlContent = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                for (XWPFRun run : paragraph.getRuns()) {
                    if (run.isBold()) {
                        htmlContent.append("<b>");
                    }
                    if (run.isItalic()) {
                        htmlContent.append("<i>");
                    }

                    htmlContent.append(run.text().replace("\t", "    ").replace("\n", "<br>"));

                    if (run.isItalic()) {
                        htmlContent.append("</i>");
                    }
                    if (run.isBold()) {
                        htmlContent.append("</b>");
                    }
                }
                htmlContent.append("<br>");
            }
        } catch (IOException e) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения файла");
        }
        return htmlContent.toString();
    }
}
