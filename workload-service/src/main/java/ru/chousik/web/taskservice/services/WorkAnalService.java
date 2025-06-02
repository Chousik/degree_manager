package ru.chousik.web.taskservice.services;

import org.apache.pdfbox.pdmodel.PDDocument;

//Сервис по анализу работ
public interface WorkAnalService {
    Integer getCompletion(PDDocument document);
    String getTitle(PDDocument document);
}
