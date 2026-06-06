package com.newsaggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "articles")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> aiResults = new HashMap<>();

    public Article() {
    }

    public Article(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getAiResults() {
        return aiResults;
    }

    public void setAiResults(Map<String, Object> aiResults) {
        this.aiResults = aiResults;
    }

    // For backwards compatibility with older Jackson deserialization if needed
    public void setAiSummary(String aiSummary) {
        this.aiResults.put("summary", aiSummary);
    }
    
    public String getAiSummary() {
        return this.aiResults.containsKey("summary") ? this.aiResults.get("summary").toString() : null;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", aiResults=" + aiResults +
                '}';
    }
}
