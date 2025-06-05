package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.web.taskservice.dto.WorkDTO;
import ru.chousik.web.taskservice.services.WorkService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/works")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkController {
    WorkService workService;

    @GetMapping()
    public List<WorkDTO> getWork(){
        return workService.getWorks();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteWork(@PathVariable UUID uuid){
        workService.deleteWork(uuid);
        return ResponseEntity.ok().build();
    }
}
