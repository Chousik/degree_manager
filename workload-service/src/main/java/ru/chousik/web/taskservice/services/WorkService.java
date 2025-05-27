package ru.chousik.web.taskservice.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.chousik.web.taskservice.dto.WorkDTO;

import java.util.List;

public interface WorkService {
    List<WorkDTO> getWorks(

    );
}
