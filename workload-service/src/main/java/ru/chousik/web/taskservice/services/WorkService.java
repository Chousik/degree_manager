package ru.chousik.web.taskservice.services;

import ru.chousik.web.taskservice.dto.SaveWorkDTO;
import ru.chousik.web.taskservice.dto.WorkDTO;

import java.net.URL;
import java.util.List;

public interface WorkService {
    List<WorkDTO> getWorks();
    void saveWork(SaveWorkDTO saveWorkDTO,
                  String key);
}
