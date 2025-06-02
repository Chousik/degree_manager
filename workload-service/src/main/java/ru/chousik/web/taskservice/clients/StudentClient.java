package ru.chousik.web.taskservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.chousik.web.dto.StudentDTO;
import ru.chousik.web.dto.TeacherDTO;
import ru.chousik.web.taskservice.config.FeignClientConfig;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}", configuration = FeignClientConfig.class)
public interface StudentClient {
    @GetMapping("/student/{id}")
    StudentDTO getStudentById(@PathVariable UUID id);
    @GetMapping("/teacher/{id}")
    TeacherDTO getTeacherById(@PathVariable UUID id);
}