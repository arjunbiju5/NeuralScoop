package com.newsaggregator.controller;

import com.newsaggregator.model.Article;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    private final ArticleRepository articleRepository;

    public NewsController(NewsService newsService, ArticleRepository articleRepository) {
        this.newsService = newsService;
        this.articleRepository = articleRepository;
    }

    @PostMapping("/aggregate")
    public ResponseEntity<List<Article>> aggregateNews(@RequestParam(defaultValue = "technology") String category) {
        List<Article> aggregatedArticles = newsService.fetchAndSummarize(category);
        return ResponseEntity.ok(aggregatedArticles);
    }

    @GetMapping
    public ResponseEntity<List<Article>> getAllNews() {
        return ResponseEntity.ok(articleRepository.findAll());
    }
}
