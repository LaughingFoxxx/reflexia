package com.project.me.central_java_service.service.file_exporters.file_exporters_impl;

import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.service.file_exporters.FileExporter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class TXTFileExporter implements FileExporter {
    @Override
    public File exportFile(DocumentToExportDTO exportDTO, Document document) {
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(document.getText());
        Elements elements = htmlDoc.body().children();

        File resultFile = new File(exportDTO.fileName() + ".txt");

        StringBuilder text = new StringBuilder();

        int extraLines = Math.max(0, (int) (exportDTO.lineSpacing() - 1));
        int leftMargin = (int) (exportDTO.leftMargin() * 2);

        // Обрабатываем элементы HTML
        int listIndex = 1; // Для нумерованных списков
        for (Element element : elements) {
            switch (element.tagName().toLowerCase()) {
                case "p":
                case "h1":
                case "h2":
                    // Добавляем текст абзаца с учетом отступов
                    String paragraphText = element.text().trim();
                    if (!paragraphText.isEmpty()) {
                        text.append(paragraphText).append("\n");
                        text.append("\n");
                        // Добавляем пустые строки для межстрочного интервала
                        text.append("\n".repeat(extraLines));
                    }
                    break;
                case "ul":
                    // Ненумерованный список
                    for (Element li : element.select("li")) {
                        String liText = li.text().trim();
                        if (!liText.isEmpty()) {
                            text.append(leftMargin).append("- ").append(liText).append("\n");
                            text.append("\n".repeat(extraLines));
                        }
                    }
                    break;
                case "ol":
                    // Нумерованный список
                    listIndex = 1;
                    for (Element li : element.select("li")) {
                        String liText = li.text().trim();
                        if (!liText.isEmpty()) {
                            text.append(leftMargin).append(listIndex).append(". ").append(liText).append("\n");
                            text.append("\n".repeat(extraLines));
                            listIndex++;
                        }
                    }
                    break;
                default:
                    // Игнорируем неподдерживаемые теги, но обрабатываем текст
                    String defaultText = element.text().trim();
                    if (!defaultText.isEmpty()) {
                        text.append(leftMargin).append(defaultText).append("\n");
                        text.append("\n".repeat(extraLines));
                    }
                    break;
            }

            // Обрабатываем отступы React-Quill (ql-indent-*)
            String indentClass = element.classNames().stream()
                    .filter(c -> c.startsWith("ql-indent-"))
                    .findFirst()
                    .orElse(null);
            if (indentClass != null) {
                int indentLevel = Integer.parseInt(indentClass.replace("ql-indent-", ""));
                String indent = " ".repeat(indentLevel * 4); // 4 пробела на уровень
                text.insert(text.lastIndexOf("\n") + 1, indent);
            }
        }

        // Записываем текст в файл с UTF-8
        String resultText = text.toString();
        try (FileOutputStream fos = new FileOutputStream(resultFile)) {
            fos.write(resultText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resultFile;
    }
}
