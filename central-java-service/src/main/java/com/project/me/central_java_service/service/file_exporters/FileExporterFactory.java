package com.project.me.central_java_service.service.file_exporters;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.service.file_exporters.file_exporters_impl.DocXFileExporter;
import com.project.me.central_java_service.service.file_exporters.file_exporters_impl.PDFileExporter;
import com.project.me.central_java_service.service.file_exporters.file_exporters_impl.TXTFileExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FileExporterFactory {
    private final ApplicationContext applicationContext;

    @Autowired
    public FileExporterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public FileExporter getFileExporter(String fileFormat) {
        return switch (fileFormat.toLowerCase()) {
            case "docx" -> applicationContext.getBean(DocXFileExporter.class);
            case "pdf" -> applicationContext.getBean(PDFileExporter.class);
            case "txt" -> applicationContext.getBean(TXTFileExporter.class);
            default -> throw new BaseCoreServiceException(HttpStatus.NOT_FOUND, "Такой формат не поддерживается");
        };
    }
}
