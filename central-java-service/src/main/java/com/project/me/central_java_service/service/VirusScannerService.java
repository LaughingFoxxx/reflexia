package com.project.me.central_java_service.service;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
public class VirusScannerService {
    private final ClamavClient clamavClient;

    public VirusScannerService() {
        this.clamavClient = new ClamavClient("localhost", 3310);
    }

    public boolean scanFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Файл не предоставлен или пустой");
        }

        try (InputStream inputStream = file.getInputStream()) {
            ScanResult.VirusFound res = (ScanResult.VirusFound) clamavClient.scan(inputStream);
            Map<String, Collection<String>> viruses = res.getFoundViruses();
            if (!viruses.isEmpty()) {
                String virusNames = String.join(",", (CharSequence) res.getFoundViruses().values());
                log.warn("VirusScannerService. Обрнаружен небезопасный файл {}, вирус(ы): {}", file.getOriginalFilename(), virusNames);
                throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Найден вирус");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
