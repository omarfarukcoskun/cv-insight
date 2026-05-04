package com.cvinsight.db.dao;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExampleCVDao {

    private final Connection conn;

    public ExampleCVDao() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Inserts a successful example CV into the example_cvs table.
     * company, role, category are metadata shown in the Browse Examples screen.
     */
    public void insert(String id, String company, String role,
                       String category, String rawText, int score) throws SQLException {
        String sql = "INSERT INTO example_cvs (id, company, role, category, raw_text, score, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, company);
            ps.setString(3, role);
            ps.setString(4, category);
            ps.setString(5, rawText);
            ps.setInt(6, score);
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public List<CV> findAll() throws SQLException {
        return queryExamples("SELECT * FROM example_cvs ORDER BY score DESC", null, null);
    }

    public List<CV> findByCategory(String category) throws SQLException {
        return queryExamples("SELECT * FROM example_cvs WHERE category = ? ORDER BY score DESC", category, null);
    }

    public List<CV> findByCompany(String company) throws SQLException {
        return queryExamples("SELECT * FROM example_cvs WHERE company = ? ORDER BY score DESC", null, company);
    }

    public List<String> findDistinctCompanies() throws SQLException {
        List<String> companies = new ArrayList<>();
        String sql = "SELECT DISTINCT company FROM example_cvs WHERE company IS NOT NULL ORDER BY company";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                companies.add(rs.getString("company"));
            }
        }
        return companies;
    }

    private List<CV> queryExamples(String sql, String category, String company) throws SQLException {
        List<CV> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (category != null) ps.setString(1, category);
            if (company != null)  ps.setString(1, company);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CV cv = new CV(
                        rs.getString("id"),
                        null,
                        rs.getString("company") + " — " + rs.getString("role"),
                        rs.getString("raw_text"),
                        null,
                        LocalDateTime.parse(rs.getString("created_at")),
                        CVStatus.ANALYZED
                    );
                    results.add(cv);
                }
            }
        }
        return results;
    }
}
