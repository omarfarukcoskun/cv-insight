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
     * Inserts an example CV.
     * Field mapping in the returned CV model:
     *   userId     = category  (for search)
     *   ownerName  = "PersonName | Company — Role"
     *   sourceFile = pdfFilename (for the View button)
     */
    public void insert(String id, String company, String role, String category,
                       String personName, String rawText, int score,
                       String pdfFilename) throws SQLException {
        String sql = """
            INSERT INTO example_cvs
              (id, company, role, category, person_name, raw_text, score, pdf_filename, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, company);
            ps.setString(3, role);
            ps.setString(4, category);
            ps.setString(5, personName);
            ps.setString(6, rawText);
            ps.setInt(7, score);
            ps.setString(8, pdfFilename);
            ps.setString(9, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM example_cvs")) {
            ps.executeUpdate();
        }
    }

    public List<CV> findAll() throws SQLException {
        return queryExamples("SELECT * FROM example_cvs ORDER BY score DESC", null, null);
    }

    public List<CV> findByCategory(String category) throws SQLException {
        return queryExamples(
            "SELECT * FROM example_cvs WHERE category = ? ORDER BY score DESC", category, null);
    }

    public List<CV> findByCompany(String company) throws SQLException {
        return queryExamples(
            "SELECT * FROM example_cvs WHERE company = ? ORDER BY score DESC", null, company);
    }

    public List<String> findDistinctCompanies() throws SQLException {
        List<String> companies = new ArrayList<>();
        String sql = "SELECT DISTINCT company FROM example_cvs WHERE company IS NOT NULL ORDER BY company";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) companies.add(rs.getString("company"));
        }
        return companies;
    }

    private List<CV> queryExamples(String sql, String category, String company) throws SQLException {
        List<CV> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (category != null) ps.setString(1, category);
            if (company  != null) ps.setString(1, company);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String personName  = rs.getString("person_name");
                    String comp        = rs.getString("company");
                    String role2       = rs.getString("role");
                    String pdfFilename = rs.getString("pdf_filename");

                    // ownerName = "PersonName | Company — Role"  (parsed by UI)
                    String ownerName = (personName != null && !personName.isBlank())
                        ? personName + " | " + comp + " — " + role2
                        : comp + " — " + role2;

                    CV cv = new CV(
                        rs.getString("id"),
                        rs.getString("category"),   // userId stores category for search
                        ownerName,
                        rs.getString("raw_text"),
                        pdfFilename,                // sourceFile stores pdf filename
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
