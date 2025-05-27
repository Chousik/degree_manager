package ru.chousik.web.taskservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.chousik.web.dto.StudentDTO;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface StudentClient {
    @GetMapping("/student/{id}")
    StudentDTO getStudentById(@PathVariable UUID id);
}