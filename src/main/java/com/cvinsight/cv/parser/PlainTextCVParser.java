package com.cvinsight.cv.parser;

import com.cvinsight.model.CV;
import com.cvinsight.model.CVStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PlainTextCVParser extends AbstractCVParser {

    @Override
    public CV parse(File file) throws CVParseException {
        if (!file.exists()) {
            throw new CVParseException("File not found: " + file.getAbsolutePath());
        }

        String rawText;
        try {
            rawText = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new CVParseException("Failed to read file: " + e.getMessage(), e);
        }

        if (rawText == null || rawText.isBlank()) {
            throw new CVParseException("File is empty: " + file.getName());
        }

        CV cv = new CV();
        cv.setOwnerName(file.getName().replace(".txt", ""));
        cv.setRawText(rawText.trim());
        cv.setSourceFile(file.getAbsolutePath());
        cv.setStatus(CVStatus.PENDING);
        cv.setSections(detectSections(rawText, null)); // cvId patched in CVService

        return cv;
    }

    @Override
    public boolean supports(String extension) {
        return "txt".equalsIgnoreCase(extension);
    }
}
