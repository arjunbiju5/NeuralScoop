package com.newsaggregator;

import com.newsaggregator.fetcher.NewsFetcher;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aggregator {
    private static final Logger logger = LoggerFactory.getLogger(Aggregator.class);

    public static void main(String[] args) {
        // Load the environment variables
        // Dotenv will look for .env in the current working directory.
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String newsApiKey = dotenv.get("NEWSAPI");
        String groqApiKey = dotenv.get("GROQAPI");

        if (newsApiKey == null || groqApiKey == null) {
            logger.error("Missing NEWSAPI or GROQAPI in environment variables (.env file).");
            return;
        }

        NewsFetcher fetcher = new NewsFetcher(newsApiKey, groqApiKey);
        
        logger.info("Starting NeuralScoop Java Backend...");
        fetcher.fetchAndSummarize("technology");
        logger.info("Finished.");
    }
}
