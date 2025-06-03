package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.chousik.web.taskservice.dto.SaveWorkDTO;
import ru.chousik.web.taskservice.services.WorkAnalService;
import ru.chousik.web.taskservice.services.WorkDocService;
import ru.chousik.web.taskservice.services.WorkService;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/work")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkDocController {
    WorkDocService workDocService;
    WorkService workService;
    WorkAnalService workAnalService;

    @PostMapping(path = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWork(@RequestPart("file") MultipartFile work,
                                        @ModelAttribute SaveWorkDTO saveWorkDTO) throws IOException {
        String key = UUID.randomUUID().toString();
        PDDocument document = Loader.loadPDF(work.getBytes());
        saveWorkDTO.setTitle(workAnalService.getTitle(document));
        saveWorkDTO.setCompletion(workAnalService.getCompletion(document));
        workDocService.uploadWork(
                key,
                work.getInputStream(),
                work.getSize(),
                work.getContentType());
        workService.saveWork(saveWorkDTO,
                key);
        System.out.println(saveWorkDTO.getTitle());
        System.out.println(saveWorkDTO.getCompletion());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<InputStreamResource> downloadWork(@PathVariable String key) {
        return workDocService.downloadWork(key);
    }
}
