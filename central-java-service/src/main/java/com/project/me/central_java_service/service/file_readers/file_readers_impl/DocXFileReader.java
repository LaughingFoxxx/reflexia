package com.project.me.central_java_service.service.file_readers.file_readers_impl;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_readers.FileReader;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class DocXFileReader implements FileReader {
    @Override
    public String readFile(MultipartFile file) {
        StringBuilder htmlContent = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {

                // Распознаем alignment текста параграфа
                ParagraphAlignment alignment = paragraph.getAlignment();
                String alignStyle;
                switch (alignment) {
                    case LEFT:
                        alignStyle = "text-align: left;";
                        break;
                    case CENTER:
                        alignStyle = "text-align: center;";
                        break;
                    case RIGHT:
                        alignStyle = "text-align: right;";
                        break;
                    case BOTH:
                        alignStyle = "text-align: justify;";
                        break;
                    default:
                        alignStyle = "text-align: left;";
                }

                // Start paragraph div with alignment
                htmlContent.append(String.format("<p style=\"%s\">", alignStyle));

                for (XWPFRun run : paragraph.getRuns()) {
                    int fontSize = run.getFontSize();
                    String fontSizeStyle = fontSize > 0 ? String.format("font-size: %dpx;", fontSize) : "";

                    // Открываем теги стиля для текущего XWPFRun
                    htmlContent.append(String.format("<span style=\"%s\">", fontSizeStyle));

                    if (run.isBold()) {
                        htmlContent.append("<strong>");
                    }
                    if (run.isItalic()) {
                        htmlContent.append("<i>");
                    }

                    htmlContent.append(run.text()
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\n", "<br>")
                            .replace("\r", "<br>")
                    );


                    if (run.isItalic()) {
                        htmlContent.append("</i>");
                    }
                    if (run.isBold()) {
                        htmlContent.append("</strong>");
                    }

                    // Закрываем span
                    htmlContent.append("</span>");
                }

                // Close paragraph div
                htmlContent.append("</p>");
            }
        } catch (IOException e) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения файла");
        }
        return htmlContent.toString();
    }
}
