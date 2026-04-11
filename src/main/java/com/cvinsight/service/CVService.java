package com.cvinsight.service;

import com.cvinsight.cv.parser.CVParseException;
import com.cvinsight.cv.parser.CVParser;
import com.cvinsight.cv.parser.CVParserFactory;
import com.cvinsight.db.dao.CVDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    public Optional<CV> getById(String id) {
        try {
            return cvDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch CV: " + e.getMessage(), e);
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
