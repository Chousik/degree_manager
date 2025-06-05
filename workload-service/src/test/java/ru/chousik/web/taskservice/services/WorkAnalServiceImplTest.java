package ru.chousik.web.taskservice.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.Test;
import ru.chousik.web.taskservice.exception.InvalidWorkFormatException;


import static org.junit.jupiter.api.Assertions.*;

class WorkAnalServiceImplTest {
    private final WorkAnalServiceImpl service = new WorkAnalServiceImpl();

    private PDDocument createDocument(String text) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            var font = PDType0Font.load(doc, new java.io.File("/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"));
            cs.setFont(font, 12);
            cs.newLineAtOffset(100, 700);
            cs.showText(text);
            cs.endText();
        }
        return doc;
    }

    @Test
    void getTitleExtractsTopic() throws Exception {
        try (PDDocument doc = createDocument("На тему: \u00ABТестовая тема\u00BB")) {
            String title = service.getTitle(doc);
            assertEquals("Тестовая тема", title);
        }
    }

    @Test
    void getTitleThrowsForMissingTopic() throws Exception {
        try (PDDocument doc = createDocument("Нет темы")) {
            assertThrows(InvalidWorkFormatException.class, () -> service.getTitle(doc));
        }
    }

    @Test
    void getCompletionReturnsZeroForNull() {
        assertEquals(0, service.getCompletion(null));
    }
}

