package com.cvinsight.service;

import com.cvinsight.cv.parser.CVParseException;
import com.cvinsight.cv.parser.CVParser;
import com.cvinsight.cv.parser.CVParserFactory;
import com.cvinsight.db.dao.CVDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;

import com.cvinsight.model.CVStatus;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CVService {

    private final CVDao cvDao;

    public CVService() {
        this.cvDao = new CVDao();
    }

    /**
     * Parses the given file, assigns identity fields, saves to DB, and returns the CV.
     *
     * Flow:
     *   CVParserFactory → correct parser → parse(file) → assign id/userId/uploadedAt → save
     *
     * @throws CVParseException if the file format is unsupported or the file is unreadable
     * @throws RuntimeException if saving to DB fails
     */
    public CV loadFromFile(File file) throws CVParseException {
        CVParser parser = CVParserFactory.getParser(file);
        CV cv = parser.parse(file);

        // Assign identity fields now that we have a parsed CV
        String cvId = UUID.randomUUID().toString();
        cv.setId(cvId);
        cv.setUserId(SessionManager.getInstance().getCurrentUser().getId());
        cv.setUploadedAt(LocalDateTime.now());

        // Patch the cvId into each section (parsers use null as placeholder)
        for (CVSection section : cv.getSections()) {
            section.setCvId(cvId);
            if (section.getId() == null) {
                section.setId(UUID.randomUUID().toString());
            }
        }

        try {
            cvDao.insert(cv);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save CV: " + e.getMessage(), e);
        }

        return cv;
    }

    /**
     * Saves a CV built manually in the editor (no file parsing).
     * Concatenates all section content as rawText, assigns identity fields, persists to DB.
     */
    public CV saveNewCV(List<CVSection> sections) {
        String cvId   = UUID.randomUUID().toString();
        String userId = SessionManager.getInstance().getCurrentUser().getId();
        String owner  = SessionManager.getInstance().getCurrentUser().getUsername();

        StringBuilder rawText = new StringBuilder();
        for (CVSection section : sections) {
            section.setCvId(cvId);
            if (section.getId() == null) {
                section.setId(UUID.randomUUID().toString());
            }
            rawText.append(section.getTitle()).append("\n")
                   .append(section.getContent()).append("\n\n");
        }

        CV cv = new CV();
        cv.setId(cvId);
        cv.setUserId(userId);
        cv.setOwnerName(owner);
        cv.setRawText(rawText.toString().trim());
        cv.setSections(sections);
        cv.setSourceFile(null);
        cv.setUploadedAt(LocalDateTime.now());
        cv.setStatus(CVStatus.PENDING);

        try {
            cvDao.insert(cv);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save CV: " + e.getMessage(), e);
        }
        return cv;
    }

    public void deleteCV(String cvId) {
        try {
            cvDao.delete(cvId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete CV: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all CVs for the currently logged-in user, newest first.
     */
    public List<CV> getHistory() {
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getId();
            return cvDao.findByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch CV history: " + e.getMessage(), e);
        }
    }
}
