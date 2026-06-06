package com.newsaggregator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.model.Article;
import com.newsaggregator.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ArticleRepository articleRepository;

    @Value("${NEWSAPI}")
    private String newsApiKey;

    @Value("${GROQAPI}")
    private String groqApiKey;

    public NewsService(ArticleRepository articleRepository, ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.articleRepository = articleRepository;
    }

    public List<Article> fetchAndSummarize(String category) {
        List<Article> articles = fetchNews(category);
        for (Article article : articles) {
            String title = article.getTitle() != null ? article.getTitle() : "";
            String desc = article.getDescription() != null ? article.getDescription() : "";
            
            logger.info("Summarizing: {}", title);
            try {
                String summary = summarizeWithGroq(title, desc);
                article.setAiSummary(summary);
                article.getAiResults().put("model", "llama-3.1-8b-instant (Groq)");
                logger.info("AI Summary: {}", summary);
            } catch (Exception e) {
                logger.error("Error summarizing article", e);
                article.getAiResults().put("error", "Summarization failed: " + e.getMessage());
            }
        }
        
        // Save to Database (Supabase)
        if (!articles.isEmpty()) {
            articleRepository.saveAll(articles);
        }
        
        return articles;
    }

    private List<Article> fetchNews(String category) {
        List<Article> articleList = new ArrayList<>();
        logger.info("--Fetching {} news--", category);

        String url = String.format("https://newsapi.org/v2/top-headlines?category=%s&language=en&pageSize=3&apiKey=%s", category, newsApiKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(response.body());

            if (!"ok".equals(rootNode.path("status").asText())) {
                logger.error("API Error: {} - {}", rootNode.path("code").asText(), rootNode.path("message").asText());
                return articleList;
            }

            JsonNode articlesNode = rootNode.path("articles");
            if (articlesNode.isArray()) {
                for (JsonNode node : articlesNode) {
                    Article article = objectMapper.treeToValue(node, Article.class);
                    // Ensure metadata is initialized (prevent null pointer exceptions)
                    if (article.getAiResults() == null) {
                        article.setAiResults(new java.util.HashMap<>());
                    }
                    articleList.add(article);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching news", e);
        }

        return articleList;
    }

    private String summarizeWithGroq(String title, String description) throws Exception {
        String url = "https://api.groq.com/openai/v1/chat/completions";
        
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "model", "llama-3.1-8b-instant",
                        "messages", List.of(
                                Map.of("role", "system", "content", "Summarize this news in exactly 2 short sentences."),
                                Map.of("role", "user", "content", "Title: " + title + "\nDescription: " + description)
                        )
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode rootNode = objectMapper.readTree(response.body());
        
        JsonNode messageNode = rootNode.path("choices").path(0).path("message").path("content");
        if (messageNode.isMissingNode()) {
            throw new RuntimeException("Unexpected response format: " + response.body());
        }
        
        return messageNode.asText().trim();
    }
}
