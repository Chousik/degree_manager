package ru.chousik.web.taskservice.dto;

import lombok.Data;

@Data
public class WorkDto {
    String title;
    String author;
    Integer year;
    Integer completion;
    String supervisor;
}
