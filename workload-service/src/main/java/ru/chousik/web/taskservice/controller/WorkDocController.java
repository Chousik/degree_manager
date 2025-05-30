package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.chousik.web.taskservice.services.WorkDocService;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/work/upload")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkDocController {
    WorkDocService workDocService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWork(@RequestPart("file") MultipartFile work) throws IOException {
        String key = UUID.randomUUID().toString();
        URL url = workDocService.uploadWork(
                key,
                work.getInputStream(),
                work.getSize(),
                work.getContentType());
        return ResponseEntity.ok(url);
    }
}
