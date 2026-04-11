package com.cvinsight.cv.parser;

import com.cvinsight.model.CV;
import com.cvinsight.model.CVStatus;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFCVParser extends AbstractCVParser {

    @Override
    public CV parse(File file) throws CVParseException {
        if (!file.exists()) {
            throw new CVParseException("File not found: " + file.getAbsolutePath());
        }

        String rawText;
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            rawText = stripper.getText(document);
        } catch (IOException e) {
            throw new CVParseException("Failed to read PDF: " + e.getMessage(), e);
        }

        if (rawText == null || rawText.isBlank()) {
            throw new CVParseException("PDF appears to be empty or image-only: " + file.getName());
        }

        // id, userId, uploadedAt, status are set by CVService
        CV cv = new CV();
        cv.setOwnerName(file.getName().replace(".pdf", ""));
        cv.setRawText(rawText.trim());
        cv.setSourceFile(file.getAbsolutePath());
        cv.setStatus(CVStatus.PENDING);
        cv.setSections(detectSections(rawText, null)); // cvId patched in CVService after id is assigned

        return cv;
    }

    @Override
    public boolean supports(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }
}
