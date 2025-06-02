package ru.chousik.web.taskservice.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkAnalServiceImpl implements WorkAnalService {
    static Pattern TOPIC_PATTERN = Pattern.compile(
            "(?i)на\\s+тему\\s*[:\\s]*[«\"“]?\\s*([^»\"”]+?)\\s*[»\"”]",  // группа 1 — сама тема
            Pattern.UNICODE_CASE | Pattern.DOTALL);

    @Override
    public Integer getCompletion(PDDocument document) {
        return 0;
    }

    @Override
    public String getTitle(PDDocument document) {
        if (document == null) {
            return "";
        }
        String firstPages = extractFirstPages(document, 2);
        String topic = findTopic(firstPages);
        if (!topic.isEmpty()) {
            return topic;
        }
        throw new IllegalArgumentException("Диплом должен быть стандартизирован");
    }

    private static String findTopic(String text) {
        Matcher m = TOPIC_PATTERN.matcher(text);
        return m.find() ? safeTrim(m.group(1)) : "";
    }

    private static String extractFirstPages(PDDocument doc, int pages) {
        try {
            var stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(pages, doc.getNumberOfPages()));
            return stripper.getText(doc);
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
