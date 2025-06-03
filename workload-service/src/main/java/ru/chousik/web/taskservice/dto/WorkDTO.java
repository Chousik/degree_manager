package ru.chousik.web.taskservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkDTO {
    UUID uuid;
    String title;
    String author;
    Integer year;
    Integer completion;
    String supervisor;
    String key;
    Integer uniqueCount;
}
