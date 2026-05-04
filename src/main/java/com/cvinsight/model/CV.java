package com.cvinsight.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CV {

    private String id;
    private String userId;
    private String ownerName;
    private String rawText;
    private List<CVSection> sections;
    private String sourceFile;
    private LocalDateTime uploadedAt;
    private CVStatus status;

    public CV() {
        this.sections = new ArrayList<>();
        this.status = CVStatus.PENDING;
    }

    public CV(String id, String userId, String ownerName, String rawText,
              String sourceFile, LocalDateTime uploadedAt, CVStatus status) {
        this.id = id;
        this.userId = userId;
        this.ownerName = ownerName;
        this.rawText = rawText;
        this.sections = new ArrayList<>();
        this.sourceFile = sourceFile;
        this.uploadedAt = uploadedAt;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public List<CVSection> getSections() { return sections; }
    public void setSections(List<CVSection> sections) { this.sections = sections; }
    public void addSection(CVSection section) { this.sections.add(section); }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public CVStatus getStatus() { return status; }
    public void setStatus(CVStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "CV{id='" + id + "', ownerName='" + ownerName + "', status=" + status + "}";
    }
}
