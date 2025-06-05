package ru.chousik.web.taskservice.services;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import ru.chousik.web.taskservice.blob.FileResource;

import java.io.InputStream;
import java.util.UUID;

public interface WorkDocService {
    void uploadWork(String key,
                    FileResource fileResource);
    ResponseEntity<InputStreamResource> downloadWork(UUID uuid);
    void deleteWorkFile(String key);
}
