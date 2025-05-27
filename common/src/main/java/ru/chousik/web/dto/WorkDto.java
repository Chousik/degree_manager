package ru.chousik.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkDto {
    String title;
    String author;
    Integer year;
    Integer completion;
    String supervisor;
}
