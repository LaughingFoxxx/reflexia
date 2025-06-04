package com.project.me.central_java_service.service;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public VirusScannerService(@Value("${servers.clamav}") String clamavBootstrap) {
        this.clamavClient = new ClamavClient(clamavBootstrap, 3310);
    }

    public boolean scanFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Файл не предоставлен или пустой");
        }

        try (InputStream inputStream = file.getInputStream()) {
            ScanResult scanResult = clamavClient.scan(inputStream);
            if (scanResult instanceof ScanResult.OK) {
                return true;
            } else if (scanResult instanceof ScanResult.VirusFound) {
                Map<String, Collection<String>> viruses = ((ScanResult.VirusFound) scanResult).getFoundViruses();
                log.warn("VirusScannerService. Обнаружены вирусы в файле {}", file.getOriginalFilename());
                throw new BaseCoreServiceException(HttpStatus.BAD_REQUEST, "Обнаружены вирусы");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
