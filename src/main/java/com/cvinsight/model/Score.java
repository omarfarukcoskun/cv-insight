package com.cvinsight.model;

import java.time.LocalDateTime;

public class Score {

    private String id;
    private String cvId;
    private int overall;
    private LocalDateTime analyzedAt;

    public Score() {}

    public Score(String id, String cvId, int overall, LocalDateTime analyzedAt) {
        this.id = id;
        this.cvId = cvId;
        this.overall = overall;
        this.analyzedAt = analyzedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCvId() { return cvId; }
    public void setCvId(String cvId) { this.cvId = cvId; }

    public int getOverall() { return overall; }
    public void setOverall(int overall) { this.overall = overall; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

    @Override
    public String toString() {
        return "Score{cvId='" + cvId + "', overall=" + overall + "}";
    }
}
