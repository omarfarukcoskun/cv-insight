package com.cvinsight.comparison;

import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;
import com.cvinsight.model.SectionType;

import java.util.HashSet;
import java.util.Set;

/**
 * PATTERN: Strategy (implementation 2)
 *
 * Weights technical sections (Skills, Projects) more heavily than others.
 * Also adds a bonus for each recognised tech keyword shared between both CVs.
 *
 * Weights:
 *   Skills     40%
 *   Projects   25%
 *   Experience 20%
 *   Education  10%
 *   Other       5%
 *
 * Good for: software engineering, data science, and other technical roles.
 */
public class TechnicalScoringStrategy extends GeneralScoringStrategy {

    private static final Set<String> TECH_KEYWORDS = Set.of(
        "java", "python", "javascript", "typescript", "kotlin", "swift", "go", "rust",
        "spring", "react", "angular", "vue", "nodejs", "django", "flask",
        "sql", "nosql", "mongodb", "postgresql", "mysql", "redis",
        "docker", "kubernetes", "aws", "azure", "gcp", "terraform", "git",
        "machinelearning", "deeplearning", "tensorflow", "pytorch",
        "restapi", "graphql", "microservices", "agile", "scrum"
    );

    @Override
    public int compare(CV userCV, CV exampleCV) {
        if (userCV.getRawText() == null || exampleCV.getRawText() == null) return 0;

        double weightedScore = 0.0;
        weightedScore += sectionScore(userCV, exampleCV, SectionType.SKILLS)     * 0.40;
        weightedScore += sectionScore(userCV, exampleCV, SectionType.PROJECTS)   * 0.25;
        weightedScore += sectionScore(userCV, exampleCV, SectionType.EXPERIENCE) * 0.20;
        weightedScore += sectionScore(userCV, exampleCV, SectionType.EDUCATION)  * 0.10;
        weightedScore += sectionScore(userCV, exampleCV, SectionType.OTHER)      * 0.05;

        int techBonus = techKeywordBonus(userCV.getRawText(), exampleCV.getRawText());

        return Math.min(100, (int) Math.round(weightedScore) + techBonus);
    }

    @Override
    public String getStrategyName() { return "Technical"; }

    private int sectionScore(CV userCV, CV exampleCV, SectionType type) {
        String userContent    = extractSection(userCV,    type);
        String exampleContent = extractSection(exampleCV, type);
        if (userContent.isEmpty() || exampleContent.isEmpty()) return 0;

        Set<String> userWords    = tokenize(userContent);
        Set<String> exampleWords = tokenize(exampleContent);
        if (userWords.isEmpty() || exampleWords.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(userWords);
        intersection.retainAll(exampleWords);

        Set<String> union = new HashSet<>(userWords);
        union.addAll(exampleWords);

        return (int) Math.round(((double) intersection.size() / union.size()) * 100);
    }

    /** Returns the content of the first section of the given type, or empty string. */
    private String extractSection(CV cv, SectionType type) {
        if (cv.getSections() == null) return "";
        return cv.getSections().stream()
            .filter(s -> s.getType() == type)
            .map(CVSection::getContent)
            .findFirst()
            .orElse("");
    }

    /** Each shared tech keyword adds 1 point, capped at 10. */
    private int techKeywordBonus(String userText, String exampleText) {
        String userLower    = userText.toLowerCase();
        String exampleLower = exampleText.toLowerCase();
        long shared = TECH_KEYWORDS.stream()
            .filter(kw -> userLower.contains(kw) && exampleLower.contains(kw))
            .count();
        return (int) Math.min(10, shared);
    }
}
