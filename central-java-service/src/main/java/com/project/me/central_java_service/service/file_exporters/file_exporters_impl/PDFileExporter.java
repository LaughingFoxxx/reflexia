package com.project.me.central_java_service.service.file_exporters.file_exporters_impl;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.entity.Document;
import com.project.me.central_java_service.service.file_exporters.FileExporter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
@Scope("prototype")
public class PDFileExporter implements FileExporter {
    @Override
    public File exportFile(DocumentToExportDTO exportDTO, Document document) {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(exportDTO.fileName(), ".pdf").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdf = new PdfDocument(writer)) {

            // Устанавливаем размер страницы и поля (в сантиметрах, преобразуем в пункты: 1 cm = 28.35 points)
            PageSize pageSize = PageSize.A4;
            pdf.setDefaultPageSize(pageSize);

            ConverterProperties properties = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();
            fontProvider.addStandardPdfFonts();
            fontProvider.addSystemFonts();
            properties.setCharset(StandardCharsets.UTF_8.name()); // Устанавливаем UTF-8
            // Добавляем безопасную обработку шрифта
            try {
                fontProvider.addFont(exportDTO.fontName());
            } catch (Exception e) {
                // Если шрифт не найден, используем стандартный
                fontProvider.addFont("Times-Roman");
            }
            properties.setFontProvider(fontProvider);
            properties.setBaseUri("");

            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(document.getText());
            Element body = htmlDoc.body();

            String css = generateCss(exportDTO);
            Element styleTag = htmlDoc.head().appendElement("style").text(css);
            htmlDoc.head().appendChild(styleTag);

            HtmlConverter.convertToPdf(htmlDoc.outerHtml(), pdf, properties);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Ошибка обработки");
        }

        return tempFile;
    }

    // Генерируем CSS для стилизации с учетом DTO и React-Quill классов
    private static String generateCss(DocumentToExportDTO dto) {
        // Преобразуем поля из сантиметров в пункты (1 см = 28.35 пункта)
        float leftMargin = dto.leftMargin() * 28.35f;
        float rightMargin = dto.rightMargin() * 28.35f;
        float topMargin = dto.topMargin() * 28.35f;
        float bottomMargin = dto.bottomMargin() * 28.35f;

        StringBuilder css = new StringBuilder();
        css.append("@page {")
                .append("margin: ").append(topMargin).append("pt ")
                .append(rightMargin).append("pt ")
                .append(bottomMargin).append("pt ")
                .append(leftMargin).append("pt;")
                .append("}");

        css.append("body {")
                .append("font-family: ").append(dto.fontName()).append(", 'Times New Roman', serif;")
                .append("font-size: ").append(dto.fontSize()).append("pt;")
                .append("line-height: ").append(dto.lineSpacing()).append(";")
                .append("margin: 0;") // Убираем дефолтные отступы body
                .append("}");

        // Стили для заголовков
        css.append("h1 { font-size: ").append(dto.fontSize() + 4).append("pt; font-weight: bold; }");
        css.append("h2 { font-size: ").append(dto.fontSize() + 2).append("pt; font-weight: bold; }");

        // Стили для React-Quill классов
        css.append(".ql-align-center { text-align: center; }");
        css.append(".ql-align-right { text-align: right; }");
        css.append(".ql-align-justify { text-align: justify; }");

        // Исправленные стили для отступов (используем text-indent и margin-left для точного контроля)
        for (int i = 1; i <= 8; i++) {
            css.append(".ql-indent-").append(i)
                    .append(" { text-indent: ").append(40).append("px; margin-left: ")
                    .append((i - 1) * 40).append("px; }");
        }

        // Стили для списков с учетом вложенности
        css.append("ul, ol { margin-left: 40px; padding-left: 0; margin-top: 0; margin-bottom: 0; }");
        css.append("ul li { list-style-type: disc; margin-bottom: 4px; }");
        css.append("ol li { list-style-type: decimal; margin-bottom: 4px; }");

        // Стили для вложенных списков
        css.append("ul ul, ol ul, ul ol, ol ol { margin-left: 40px; }");

        // Стили для форматирования текста
        css.append("strong { font-weight: bold; }");
        css.append("em { font-style: italic; }");
        css.append("u { text-decoration: underline; }");

        return css.toString();
    }
}
