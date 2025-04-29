package com.project.me.central_java_service.service.file_readers;

import org.springframework.web.multipart.MultipartFile;

public interface FileReader {
    String readFile(MultipartFile file);
}
