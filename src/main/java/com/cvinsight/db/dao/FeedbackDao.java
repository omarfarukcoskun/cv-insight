package com.cvinsight.db.dao;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.model.Feedback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class FeedbackDao {

    private final Connection conn;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>(){}.getType();

    public FeedbackDao() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void insert(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedbacks (id, cv_id, strengths, weaknesses, suggestions) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedback.getId());
            ps.setString(2, feedback.getCvId());
            ps.setString(3, gson.toJson(feedback.getStrengths()));
            ps.setString(4, gson.toJson(feedback.getWeaknesses()));
            ps.setString(5, gson.toJson(feedback.getSuggestions()));
            ps.executeUpdate();
        }
    }

    public Optional<Feedback> findByCvId(String cvId) throws SQLException {
        String sql = "SELECT * FROM feedbacks WHERE cv_id = ?";
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

    private Feedback mapRow(ResultSet rs) throws SQLException {
        List<String> strengths   = gson.fromJson(rs.getString("strengths"),   listType);
        List<String> weaknesses  = gson.fromJson(rs.getString("weaknesses"),  listType);
        List<String> suggestions = gson.fromJson(rs.getString("suggestions"), listType);
        return new Feedback(
            rs.getString("id"),
            rs.getString("cv_id"),
            strengths,
            weaknesses,
            suggestions
        );
    }
}
