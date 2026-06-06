package com.cvinsight.db.dao;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;
import com.cvinsight.model.CVStatus;
import com.cvinsight.model.SectionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class CVDao {

    private final Connection conn;

    public CVDao() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void insert(CV cv) throws SQLException {
        String sql = "INSERT INTO cvs (id, user_id, owner_name, raw_text, source_file, uploaded_at, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cv.getId());
            ps.setString(2, cv.getUserId());
            ps.setString(3, cv.getOwnerName());
            ps.setString(4, cv.getRawText());
            ps.setString(5, cv.getSourceFile());
            ps.setString(6, cv.getUploadedAt().toString());
            ps.setString(7, cv.getStatus().name());
            ps.executeUpdate();
        }
        insertSections(cv.getId(), cv.getSections());
    }

    public void delete(String cvId) throws SQLException {
        // Delete related records first (no ON DELETE CASCADE in SQLite by default)
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cv_sections WHERE cv_id = ?")) {
            ps.setString(1, cvId); ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM scores WHERE cv_id = ?")) {
            ps.setString(1, cvId); ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM feedbacks WHERE cv_id = ?")) {
            ps.setString(1, cvId); ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cvs WHERE id = ?")) {
            ps.setString(1, cvId); ps.executeUpdate();
        }
    }

    public void updateStatus(String cvId, CVStatus status) throws SQLException {
        String sql = "UPDATE cvs SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, cvId);
            ps.executeUpdate();
        }
    }

    public List<CV> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM cvs WHERE user_id = ? ORDER BY uploaded_at DESC";
        List<CV> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CV cv = mapRow(rs);
                    cv.setSections(findSections(cv.getId()));
                    results.add(cv);
                }
            }
        }
        return results;
    }

    private void insertSections(String cvId, List<CVSection> sections) throws SQLException {
        if (sections == null || sections.isEmpty()) return;
        String sql = "INSERT INTO cv_sections (id, cv_id, section_type, title, content) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CVSection section : sections) {
                ps.setString(1, section.getId());
                ps.setString(2, cvId);
                ps.setString(3, section.getType().name());
                ps.setString(4, section.getTitle());
                ps.setString(5, section.getContent());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<CVSection> findSections(String cvId) throws SQLException {
        String sql = "SELECT * FROM cv_sections WHERE cv_id = ?";
        List<CVSection> sections = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cvId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sections.add(new CVSection(
                        rs.getString("id"),
                        rs.getString("cv_id"),
                        SectionType.valueOf(rs.getString("section_type")),
                        rs.getString("title"),
                        rs.getString("content")
                    ));
                }
            }
        }
        return sections;
    }

    private CV mapRow(ResultSet rs) throws SQLException {
        return new CV(
            rs.getString("id"),
            rs.getString("user_id"),
            rs.getString("owner_name"),
            rs.getString("raw_text"),
            rs.getString("source_file"),
            LocalDateTime.parse(rs.getString("uploaded_at")),
            CVStatus.valueOf(rs.getString("status"))
        );
    }
}
