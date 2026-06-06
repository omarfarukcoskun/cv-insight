package com.cvinsight.cv.parser;

import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;
import com.cvinsight.model.SectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Base class for all parsers.
 * Concrete parsers (PDF, TXT) handle text extraction.
 * This class handles the shared section detection logic applied to that text.
 */
public abstract class AbstractCVParser implements CVParser {

    // Keywords that signal the start of each section type.
    // Checked case-insensitively against each line.
    private static final List<SectionKeywords> SECTION_MAP = List.of(
        new SectionKeywords(SectionType.SUMMARY,    "summary", "profile", "objective", "about me", "about"),
        new SectionKeywords(SectionType.EXPERIENCE, "experience", "work experience", "employment", "work history", "professional experience"),
        new SectionKeywords(SectionType.EDUCATION,  "education", "academic", "qualifications", "academic background"),
        new SectionKeywords(SectionType.SKILLS,     "skills", "technical skills", "core competencies", "competencies", "technologies"),
        new SectionKeywords(SectionType.PROJECTS,   "projects", "personal projects", "side projects", "portfolio")
    );

    /**
     * Splits rawText into CVSection objects by looking for known section headers.
     * Lines that match no known header are collected into an OTHER section.
     */
    protected List<CVSection> detectSections(String rawText, String cvId) {
        List<CVSection> sections = new ArrayList<>();
        String[] lines = rawText.split("\\r?\\n");

        SectionType currentType = SectionType.OTHER;
        String currentTitle = "Other";
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            SectionType detected = detectSectionType(line.trim());
            if (detected != null && !detected.equals(currentType)) {
                // Save the section we were building
                if (!currentContent.isEmpty()) {
                    sections.add(buildSection(cvId, currentType, currentTitle, currentContent.toString().trim()));
                }
                currentType = detected;
                currentTitle = line.trim();
                currentContent = new StringBuilder();
            } else {
                currentContent.append(line).append("\n");
            }
        }

        // Don't forget the last section
        if (!currentContent.isEmpty()) {
            sections.add(buildSection(cvId, currentType, currentTitle, currentContent.toString().trim()));
        }

        return sections;
    }

    private SectionType detectSectionType(String line) {
        if (line.isBlank()) return null;
        String lower = line.toLowerCase();
        for (SectionKeywords sk : SECTION_MAP) {
            for (String keyword : sk.keywords()) {
                if (lower.equals(keyword) || lower.startsWith(keyword + " ") || lower.startsWith(keyword + ":")) {
                    return sk.type();
                }
            }
        }
        return null;
    }

    private CVSection buildSection(String cvId, SectionType type, String title, String content) {
        return new CVSection(UUID.randomUUID().toString(), cvId, type, title, content);
    }

    // Simple record to pair a SectionType with its trigger keywords
    private record SectionKeywords(SectionType type, String... keywords) {}
}
