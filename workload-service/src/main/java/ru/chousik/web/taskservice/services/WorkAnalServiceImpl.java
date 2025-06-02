package ru.chousik.web.taskservice.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkAnalServiceImpl implements WorkAnalService {
    @Override
    public Integer getCompletion(PDDocument document) {
        return 0;
    }

    @Override
    public String getTitle(PDDocument document) {
        if (document == null) {
            return "";
        }

        PDDocumentInformation info = document.getDocumentInformation();
        String title = info.getTitle();
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        try {
            var stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String firstPage = stripper.getText(document).trim();
            int nl = firstPage.indexOf('\n');
            if (nl > 0) {
                return firstPage.substring(0, nl).trim();
            }
            return firstPage;
        } catch (Exception ignore) {}

        return "";
    }
}
