package com.cvinsight.db.dao;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.model.Score;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class ScoreDao {

    private final Connection conn;

    public ScoreDao() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void insert(Score score) throws SQLException {
        String sql = "INSERT INTO scores (id, cv_id, overall, analyzed_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, score.getId());
            ps.setString(2, score.getCvId());
            ps.setInt(3, score.getOverall());
            ps.setString(4, score.getAnalyzedAt().toString());
            ps.executeUpdate();
        }
    }

    public Optional<Score> findByCvId(String cvId) throws SQLException {
        String sql = "SELECT * FROM scores WHERE cv_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cvId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Score mapRow(ResultSet rs) throws SQLException {
        return new Score(
            rs.getString("id"),
            rs.getString("cv_id"),
            rs.getInt("overall"),
            LocalDateTime.parse(rs.getString("analyzed_at"))
        );
    }
}
