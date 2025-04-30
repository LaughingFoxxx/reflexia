package com.project.me.central_java_service.service.file_exporters.file_exporters_impl;

import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.service.file_exporters.FileExporter;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;

@Component
@Scope("prototype")
public class DocXFileExporter implements FileExporter {
    @Override
    public File exportFile(DocumentToExportDTO exportDTO,
                           com.project.me.central_java_service.model.entity.Document userDocument) {
        try (XWPFDocument docx = new XWPFDocument()) {
            // Устанавливаем поля документа (в сантиметрах, преобразуем в twips)
            CTSectPr sectPr = docx.getDocument().getBody().addNewSectPr();
            CTPageMar pageMar = sectPr.addNewPgMar();

            int leftMargin = (int) (exportDTO.leftMargin() * 567); // 1 cm = 567 twips
            int rightMargin = (int) (exportDTO.rightMargin() * 567);
            int topMargin = (int) (exportDTO.topMargin() * 567);
            int bottomMargin = (int) (exportDTO.bottomMargin() * 567);

            pageMar.setLeft(BigInteger.valueOf(leftMargin));
            pageMar.setRight(BigInteger.valueOf(rightMargin));
            pageMar.setTop(BigInteger.valueOf(topMargin));
            pageMar.setBottom(BigInteger.valueOf(bottomMargin));

            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(userDocument.getText());
            Elements elements = htmlDoc.body().children();

            // Обрабатываем каждый элемент HTML
            for (Element element : elements) {
                XWPFParagraph paragraph = docx.createParagraph();
                XWPFRun run = paragraph.createRun();

                CTSpacing spacing = paragraph.getCTPPr().addNewSpacing();
                spacing.setLine((int) (exportDTO.lineSpacing() * 240));
                spacing.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.AUTO);

                // Устанавливаем шрифт и размер по умолчанию
                run.setFontFamily(exportDTO.fontName());
                run.setFontSize(exportDTO.fontSize());

                // Обрабатываем классы Quill для выравнивания
                if (element.hasClass("ql-align-center")) {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                } else if (element.hasClass("ql-align-right")) {
                    paragraph.setAlignment(ParagraphAlignment.RIGHT);
                } else if (element.hasClass("ql-align-justify")) {
                    paragraph.setAlignment(ParagraphAlignment.BOTH);
                }

                // Обрабатываем отступы (ql-indent-*)
                String indentClass = element.classNames().stream()
                        .filter(c -> c.startsWith("ql-indent-"))
                        .findFirst()
                        .orElse(null);
                if (indentClass != null) {
                    int indentLevel = Integer.parseInt(indentClass.replace("ql-indent-", ""));
                    paragraph.setIndentationLeft(indentLevel * 720); // 720 twips = 1/2 inch
                }

                // Обрабатываем тип элемента
                switch (element.tagName().toLowerCase()) {
                    case "p":
                        processText(run, element, exportDTO);
                        break;
                    case "strong":
                        run.setBold(true);
                        processText(run, element, exportDTO);
                        break;
                    case "em":
                        run.setItalic(true);
                        processText(run, element, exportDTO);
                        break;
                    case "u":
                        run.setUnderline(UnderlinePatterns.SINGLE);
                        processText(run, element, exportDTO);
                        break;
                    case "h1":
                        run.setFontSize(exportDTO.fontSize() + 4);
                        run.setBold(true);
                        processText(run, element, exportDTO);
                        break;
                    case "h2":
                        run.setFontSize(exportDTO.fontSize() + 2);
                        run.setBold(true);
                        processText(run, element, exportDTO);
                        break;
                    case "li":
                        // Для списков создаем новый параграф с отступом
                        paragraph.setIndentationLeft(720); // Эмуляция списка
                        run.setText("• "); // Добавляем маркер
                        processText(run, element, exportDTO);
                        break;
                    case "ol":
                    case "ul":
                        // Обрабатываем элементы списка рекурсивно
                        for (Element li : element.children()) {
                            if (li.tagName().equalsIgnoreCase("li")) {
                                XWPFParagraph liParagraph = docx.createParagraph();
                                XWPFRun liRun = liParagraph.createRun();
                                liRun.setFontFamily(exportDTO.fontName());
                                liRun.setFontSize(exportDTO.fontSize());
                                liParagraph.setIndentationLeft(720);
                                liRun.setText("• ");
                                processText(liRun, li, exportDTO);
                            }
                        }
                        continue; // Пропускаем дальнейшую обработку
                    default:
                        processText(run, element, exportDTO);
                        break;
                }
            }

            File tempFile = Files.createTempFile(exportDTO.fileName(), ".docx").toFile();

            // Сохраняем документ в файл
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                docx.write(out);
            }

            // Закрываем документ
            docx.close();

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processText(XWPFRun run, Element element, DocumentToExportDTO dto) {
        String text = element.ownText();
        if (!text.isEmpty()) {
            run.setText(text);
        }

        // Рекурсивно обрабатываем дочерние элементы
        for (Element child : element.children()) {
            XWPFRun childRun = run.getParagraph().createRun();
            childRun.setFontFamily(dto.fontName());
            childRun.setFontSize(dto.fontSize());

            switch (child.tagName().toLowerCase()) {
                case "strong":
                    childRun.setBold(true);
                    childRun.setText(child.ownText());
                    break;
                case "em":
                    childRun.setItalic(true);
                    childRun.setText(child.ownText());
                    break;
                case "u":
                    childRun.setUnderline(UnderlinePatterns.SINGLE);
                    childRun.setText(child.ownText());
                    break;
                default:
                    childRun.setText(child.ownText());
                    break;
            }
        }
    }
}
