package com.cvinsight.service;

import com.cvinsight.comparison.ComparisonEngine;
import com.cvinsight.comparison.ComparisonResult;
import com.cvinsight.comparison.GeneralScoringStrategy;
import com.cvinsight.comparison.TechnicalScoringStrategy;
import com.cvinsight.db.dao.ExampleCVDao;
import com.cvinsight.model.CV;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class ComparisonService {

    private final ExampleCVDao    exampleCVDao;
    private final ComparisonEngine engine;

    public ComparisonService() {
        this.exampleCVDao = new ExampleCVDao();
        this.engine       = new ComparisonEngine(new TechnicalScoringStrategy());
    }

    /**
     * Switches the active scoring strategy.
     * "technical" → TechnicalScoringStrategy
     * anything else → GeneralScoringStrategy
     */
    public void useStrategy(String strategyName) {
        if ("technical".equalsIgnoreCase(strategyName)) {
            engine.setStrategy(new TechnicalScoringStrategy());
        } else {
            engine.setStrategy(new GeneralScoringStrategy());
        }
    }

    public String getCurrentStrategyName() {
        return engine.getStrategy().getStrategyName();
    }

    /**
     * Returns the top N example CVs ranked by similarity to the given CV.
     *
     * @param userCV the user's CV to compare against
     * @param limit  max number of results to return
     */
    public List<ComparisonResult> getTopMatches(CV userCV, int limit) {
        try {
            List<CV> examples = exampleCVDao.findAll();
            return examples.stream()
                .map(example -> engine.compare(userCV, example))
                .sorted(Comparator.comparingInt(ComparisonResult::getSimilarityScore).reversed())
                .limit(limit)
                .toList();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load example CVs: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all example CVs with their similarity score against userCV.
     */
    public List<ComparisonResult> compareAll(CV userCV) {
        return getTopMatches(userCV, Integer.MAX_VALUE);
    }

    /**
     * Returns all example CVs from the DB, unscored (for the Browse screen
     * when no user CV is selected yet).
     */
    public List<CV> getAllExamples() {
        try {
            return exampleCVDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load examples: " + e.getMessage(), e);
        }
    }

    public List<CV> getExamplesByCategory(String category) {
        try {
            return exampleCVDao.findByCategory(category);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load examples: " + e.getMessage(), e);
        }
    }

    public List<CV> getExamplesByCompany(String company) {
        try {
            return exampleCVDao.findByCompany(company);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load examples: " + e.getMessage(), e);
        }
    }
}
