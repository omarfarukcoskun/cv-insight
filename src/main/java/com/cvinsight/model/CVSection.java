package com.cvinsight.model;

public class CVSection {

    private String id;
    private String cvId;
    private SectionType type;
    private String title;
    private String content;

    public CVSection() {}

    public CVSection(String id, String cvId, SectionType type, String title, String content) {
        this.id = id;
        this.cvId = cvId;
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCvId() { return cvId; }
    public void setCvId(String cvId) { this.cvId = cvId; }

    public SectionType getType() { return type; }
    public void setType(SectionType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "CVSection{type=" + type + ", title='" + title + "'}";
    }
}
