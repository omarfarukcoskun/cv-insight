package com.cvinsight.model;

import java.util.ArrayList;
import java.util.List;

public class Feedback {

    private String id;
    private String cvId;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;

    public Feedback() {
        this.strengths = new ArrayList<>();
        this.weaknesses = new ArrayList<>();
        this.suggestions = new ArrayList<>();
    }

    public Feedback(String id, String cvId,
                    List<String> strengths,
                    List<String> weaknesses,
                    List<String> suggestions) {
        this.id = id;
        this.cvId = cvId;
        this.strengths = strengths != null ? strengths : new ArrayList<>();
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCvId() { return cvId; }
    public void setCvId(String cvId) { this.cvId = cvId; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    @Override
    public String toString() {
        return "Feedback{cvId='" + cvId + "', strengths=" + strengths.size()
                + ", weaknesses=" + weaknesses.size()
                + ", suggestions=" + suggestions.size() + "}";
    }
}
