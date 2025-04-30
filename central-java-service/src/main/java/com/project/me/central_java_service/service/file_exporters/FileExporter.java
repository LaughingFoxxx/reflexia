package com.project.me.central_java_service.service.file_exporters;

import com.project.me.central_java_service.model.dto.DocumentToExportDTO;
import com.project.me.central_java_service.model.entity.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;

public interface FileExporter {
    File exportFile(DocumentToExportDTO exportDTO, Document document);
}
