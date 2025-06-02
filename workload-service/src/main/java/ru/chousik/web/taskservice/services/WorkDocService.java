package ru.chousik.web.taskservice.services;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;

public interface WorkDocService {
    void uploadWork(String key,
        InputStream data,
        long length,
        String contType);
    ResponseEntity<InputStreamResource> downloadWork(String key);
}
