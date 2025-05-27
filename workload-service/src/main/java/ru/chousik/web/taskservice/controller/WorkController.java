package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.taskservice.dto.WorkDTO;
import ru.chousik.web.taskservice.services.WorkService;

import java.util.List;

@RestController
@RequestMapping("/work")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkController {
    WorkService workService;

    @GetMapping()
    public List<WorkDTO> getWorks(){
        return workService.getWorks();
    }
}
