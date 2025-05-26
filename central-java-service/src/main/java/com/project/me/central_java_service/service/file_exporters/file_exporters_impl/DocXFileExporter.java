package com.project.me.central_java_service.service.file_exporters.file_exporters_impl;

import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.service.file_exporters.FileExporter;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;

@Component
public class DocXFileExporter implements FileExporter {
    @Override
    public File exportFile(DocumentToExportDTO exportDTO, Document userDocument) {
        try (XWPFDocument docx = new XWPFDocument()) {
            // Поля документа
            CTSectPr sectPr = docx.getDocument().getBody().addNewSectPr();
            CTPageMar pageMar = sectPr.addNewPgMar();

            int leftMargin = (int) (exportDTO.leftMargin() * 567);
            int rightMargin = (int) (exportDTO.rightMargin() * 567);
            int topMargin = (int) (exportDTO.topMargin() * 567);
            int bottomMargin = (int) (exportDTO.bottomMargin() * 567);

            pageMar.setLeft(BigInteger.valueOf(leftMargin));
            pageMar.setRight(BigInteger.valueOf(rightMargin));
            pageMar.setTop(BigInteger.valueOf(topMargin));
            pageMar.setBottom(BigInteger.valueOf(bottomMargin));

            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(userDocument.getText());
            Elements elements = htmlDoc.body().children();

            // HTML
            for (Element element : elements) {
                XWPFParagraph paragraph = docx.createParagraph();
                CTSpacing spacing = paragraph.getCTPPr().addNewSpacing();
                spacing.setLine((int) (exportDTO.lineSpacing() * 240));
                spacing.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.AUTO);

                // Выравнивание
                if (element.hasClass("ql-align-center")) {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                } else if (element.hasClass("ql-align-right")) {
                    paragraph.setAlignment(ParagraphAlignment.RIGHT);
                } else if (element.hasClass("ql-align-justify")) {
                    paragraph.setAlignment(ParagraphAlignment.BOTH);
                }

                // Отступы
                String indentClass = element.classNames().stream()
                        .filter(c -> c.startsWith("ql-indent-"))
                        .findFirst()
                        .orElse(null);
                if (indentClass != null) {
                    int indentLevel = Integer.parseInt(indentClass.replace("ql-indent-", ""));
                    paragraph.setIndentationLeft(indentLevel * 720);
                }

                // Тип
                switch (element.tagName().toLowerCase()) {
                    case "p":
                    case "h1":
                    case "h2":
                    case "li":
                        processElement(paragraph, element, exportDTO);
                        break;
                    case "ol":
                    case "ul":
                        for (Element li : element.children()) {
                            if (li.tagName().equalsIgnoreCase("li")) {
                                XWPFParagraph liParagraph = docx.createParagraph();
                                liParagraph.setIndentationLeft(720);
                                processElement(liParagraph, li, exportDTO, true);
                            }
                        }
                        continue;
                    default:
                        processElement(paragraph, element, exportDTO);
                        break;
                }
            }

            File tempFile = Files.createTempFile(exportDTO.fileName(), ".docx").toFile();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                docx.write(out);
            }

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processElement(XWPFParagraph paragraph, Element element, DocumentToExportDTO dto) {
        processElement(paragraph, element, dto, false);
    }

    private void processElement(XWPFParagraph paragraph, Element element, DocumentToExportDTO dto, boolean isListItem) {
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(dto.fontName());
        run.setFontSize(dto.fontSize());

        // Стили для заголовков
        if (element.tagName().equalsIgnoreCase("h1")) {
            run.setFontSize(dto.fontSize() + 4);
            run.setBold(true);
        } else if (element.tagName().equalsIgnoreCase("h2")) {
            run.setFontSize(dto.fontSize() + 2);
            run.setBold(true);
        }

        // Маркер для списка
        if (isListItem) {
            run.setText("• ");
        }

        // Узлы с учетом форматирования
        processNodes(paragraph, element.childNodes(), dto, new Formatting());
    }

    // Класс для хранения текущего форматирования
    private static class Formatting {
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;

        Formatting copy() {
            Formatting copy = new Formatting();
            copy.bold = this.bold;
            copy.italic = this.italic;
            copy.underline = this.underline;
            return copy;
        }
    }

    private void processNodes(XWPFParagraph paragraph, java.util.List<Node> nodes, DocumentToExportDTO dto, Formatting formatting) {
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text();
                if (!text.isEmpty()) {
                    XWPFRun run = paragraph.createRun();
                    run.setFontFamily(dto.fontName());
                    run.setFontSize(dto.fontSize());
                    run.setBold(formatting.bold);
                    run.setItalic(formatting.italic);
                    run.setUnderline(formatting.underline ? UnderlinePatterns.SINGLE : UnderlinePatterns.NONE);
                    run.setText(text);
                }
            } else if (node instanceof Element) {
                Element child = (Element) node;
                // Копия текущего форматирования
                Formatting childFormatting = formatting.copy();

                // Применяем форматирование для текущего элемента
                switch (child.tagName().toLowerCase()) {
                    case "strong":
                        childFormatting.bold = true;
                        break;
                    case "em":
                        childFormatting.italic = true;
                        break;
                    case "u":
                        childFormatting.underline = true;
                        break;
                }

                processNodes(paragraph, child.childNodes(), dto, childFormatting);
            }
        }
    }
}