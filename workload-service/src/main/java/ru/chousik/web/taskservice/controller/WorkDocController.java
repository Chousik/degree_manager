package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.chousik.web.taskservice.blob.FileResource;
import ru.chousik.web.taskservice.dto.SaveWorkDTO;
import ru.chousik.web.taskservice.services.WorkAnalService;
import ru.chousik.web.taskservice.services.WorkDocService;
import ru.chousik.web.taskservice.services.WorkService;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/works")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkDocController {
    WorkDocService workDocService;
    WorkService workService;
    WorkAnalService workAnalService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWork(@RequestPart("file") MultipartFile work,
                                        @ModelAttribute SaveWorkDTO saveWorkDTO) throws IOException {
        String key = UUID.randomUUID().toString();
        try (PDDocument document = Loader.loadPDF(work.getBytes())) {
            saveWorkDTO.setTitle(workAnalService.getTitle(document));
            saveWorkDTO.setCompletion(workAnalService.getCompletion(document));
        }
        try (FileResource file = new FileResource(
                work.getInputStream(),
                work.getSize(),
                work.getContentType())){
            workDocService.uploadWork(key, file);
        }

        try {
            workService.saveWork(saveWorkDTO, key);
        } catch (Exception ex) {
            workDocService.deleteWorkFile(key);
            throw ex;
        }
        return ResponseEntity.status(201).build();
    }

    @PutMapping(
            path = "/{uuid}/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateWorkFile(
            @PathVariable UUID uuid,
            @RequestPart("file") MultipartFile work
    ) throws IOException {
        String key = UUID.randomUUID().toString();
        try (FileResource file = new FileResource(
                work.getInputStream(),
                work.getSize(),
                work.getContentType())){
            workDocService.uploadWork(key, file);
        }
        SaveWorkDTO saveWorkDTO = new SaveWorkDTO();
        try (PDDocument document = Loader.loadPDF(work.getBytes())) {
            saveWorkDTO.setTitle(workAnalService.getTitle(document));
            saveWorkDTO.setCompletion(workAnalService.getCompletion(document));
        }

        try {
            workService.updateWork(uuid, saveWorkDTO, key);
        } catch (Exception ex) {
            workDocService.deleteWorkFile(key);
            throw ex;
        }
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity<InputStreamResource> downloadWork(@PathVariable UUID uuid) {
        return workDocService.downloadWork(uuid);
    }
}
