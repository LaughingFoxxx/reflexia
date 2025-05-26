package com.project.me.central_java_service.service.file_readers.file_readers_impl;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_readers.FileReader;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
@Scope("prototype")
public class DocFileReader implements FileReader {
    @Override
    public String readFile(MultipartFile file) {
        StringBuilder htmlContent = new StringBuilder("<html><body>");

        try (InputStream inputStream = file.getInputStream();
             HWPFDocument document = new HWPFDocument(inputStream)) {
            Range range = document.getRange();

            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph paragraph = range.getParagraph(i);

                ParagraphAlignment paragraphAlignment = ParagraphAlignment.valueOf(paragraph.getFontAlignment());
                String alignment;
                switch (paragraphAlignment) {
                    case LEFT -> alignment = "text-align: left;";
                    case RIGHT -> alignment = "text-align: right;";
                    case BOTH -> alignment = "text-align: justify";
                    case CENTER -> alignment = "text-align: center;";
                    default -> alignment = "text-align: left;";
                }

                htmlContent.append(String.format("<p style=\"%s\">", alignment));

                for (int j = 0; j < paragraph.numCharacterRuns(); j++) {
                    CharacterRun run = paragraph.getCharacterRun(j);

                    int fontSize = run.getFontSize();
                    String fontSizeStyle = fontSize > 0 ? String.format("font-size: %dpx", fontSize) : "";

                    htmlContent.append(String.format("<span style=\"%s\">", fontSizeStyle));

                    String text = run.text();

                    if (run.isBold()) {
                        htmlContent.append("<b>");
                    }
                    if (run.isItalic()) {
                        htmlContent.append("<i>");
                    }

                    htmlContent.append(text
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
                        htmlContent.append("</b>");
                    }

                    htmlContent.append("/span");
                }
                htmlContent.append("</p><br>");
            }
        } catch (IOException e) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка чтения doc файла");
        }
        return htmlContent.toString();
    }
}
