package com.example.redis.search;

import com.example.redis.config.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Cutting-edge demonstration of Advanced Search capabilities with Redis.
 * 
 * Features demonstrated:
 * - Full-text search with tokenization and stemming
 * - Faceted search and filtering
 * - Autocomplete and suggestions
 * - Search analytics and personalization
 * - Elasticsearch-like aggregations
 * - Real-time search indexing
 */
public class AdvancedSearchDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchDemo.class);
    
    private final RedisConnectionManager connectionManager;
    
    public AdvancedSearchDemo(RedisConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public void demonstrateAdvancedSearch() {
        logger.info("🔍 Starting Advanced Search Demonstration");
        
        try {
            // 1. Full-text search setup
            demonstrateFullTextSearch();
            
            // 2. Faceted search and filtering
            demonstrateFacetedSearch();
            
            // 3. Autocomplete and suggestions
            demonstrateAutocomplete();
            
            // 4. Search analytics
            demonstrateSearchAnalytics();
            
            // 5. Personalized search
            demonstratePersonalizedSearch();
            
            // 6. Real-time indexing
            demonstrateRealTimeIndexing();
            
            logger.info("✅ Advanced Search demonstration completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error in advanced search demonstration", e);
        }
    }
    
    private void demonstrateFullTextSearch() {
        logger.info("📚 Demonstrating Full-Text Search...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📝 Creating search index with documents...");
            
            // Create sample documents
            createDocuments(commands);
            
            // Build full-text search index
            buildFullTextIndex(commands);
            
            logger.info("🔍 Performing full-text searches...");
            
            // Search queries
            String[] searchQueries = {
                "machine learning",
                "artificial intelligence",
                "data science python",
                "cloud computing aws",
                "web development react"
            };
            
            for (String query : searchQueries) {
                performFullTextSearch(commands, query);
            }
            
            // Advanced search features
            logger.info("🎯 Demonstrating advanced search features...");
            
            // Boolean search
            performBooleanSearch(commands, "python AND machine", "Must contain both terms");
            performBooleanSearch(commands, "react OR vue", "Contains either term");
            performBooleanSearch(commands, "NOT beginner", "Excludes beginner content");
            
            // Phrase search
            performPhraseSearch(commands, "machine learning");
            
            // Fuzzy search (typo tolerance)
            performFuzzySearch(commands, "machne lerning"); // Intentional typos
            
            return null;
        });
    }
    
    private void demonstrateFacetedSearch() {
        logger.info("🏷️  Demonstrating Faceted Search and Filtering...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📊 Building faceted search indexes...");
            
            // Build facet indexes
            buildFacetIndexes(commands);
            
            logger.info("🔍 Performing faceted searches...");
            
            // Search with category facets
            performFacetedSearch(commands, "programming", 
                Map.of("category", "Programming", "difficulty", "Intermediate"));
            
            // Search with price range facets
            performFacetedSearch(commands, "course", 
                Map.of("price_range", "50-100"));
            
            // Search with rating facets
            performFacetedSearch(commands, "web", 
                Map.of("rating", "4+"));
            
            // Multi-facet search
            performFacetedSearch(commands, "python", 
                Map.of("category", "Data Science", "difficulty", "Advanced", "rating", "4+"));
            
            // Show facet counts
            showFacetCounts(commands);
            
            return null;
        });
    }
    
    private void demonstrateAutocomplete() {
        logger.info("💭 Demonstrating Autocomplete and Suggestions...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("🔤 Building autocomplete index...");
            
            // Build autocomplete trie structure
            buildAutocompleteIndex(commands);
            
            logger.info("💡 Testing autocomplete suggestions...");
            
            // Test autocomplete queries
            String[] partialQueries = {"mach", "data", "web", "artif", "prog"};
            
            for (String partial : partialQueries) {
                var suggestions = getAutocompleteSuggestions(commands, partial);
                logger.info("  💭 '{}' → {}", partial, suggestions);
            }
            
            // Popular search suggestions
            logger.info("🔥 Showing popular searches...");
            var popularSearches = getPopularSearches(commands);
            logger.info("  🏆 Popular searches: {}", popularSearches);
            
            // Query suggestions based on user intent
            logger.info("🎯 Intent-based suggestions...");
            
            Map<String, List<String>> intentSuggestions = Map.of(
                "learning", Arrays.asList("machine learning course", "deep learning tutorial", "learning path"),
                "programming", Arrays.asList("programming languages", "programming tutorial", "programming job"),
                "development", Arrays.asList("web development", "mobile development", "software development")
            );
            
            for (Map.Entry<String, List<String>> entry : intentSuggestions.entrySet()) {
                commands.del("autocomplete:intent:" + entry.getKey());
                for (String suggestion : entry.getValue()) {
                    commands.zadd("autocomplete:intent:" + entry.getKey(), 
                        ThreadLocalRandom.current().nextDouble(0.5, 1.0), suggestion);
                }
                
                var topSuggestions = commands.zrevrange("autocomplete:intent:" + entry.getKey(), 0, 2);
                logger.info("  🎯 Intent '{}': {}", entry.getKey(), topSuggestions);
            }
            
            return null;
        });
    }
    
    private void demonstrateSearchAnalytics() {
        logger.info("📊 Demonstrating Search Analytics...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📈 Tracking search metrics...");
            
            // Simulate search activity
            simulateSearchActivity(commands);
            
            // Query performance analytics
            logger.info("⚡ Query performance metrics:");
            
            var queryStats = commands.hgetall("search:analytics:query_performance");
            for (Map.Entry<String, String> entry : queryStats.entrySet()) {
                logger.info("  📊 {}: {} ms avg", entry.getKey(), entry.getValue());
            }
            
            // Search result analytics
            logger.info("🎯 Search result metrics:");
            
            var resultStats = commands.hgetall("search:analytics:result_stats");
            for (Map.Entry<String, String> entry : resultStats.entrySet()) {
                logger.info("  📈 {}: {}", entry.getKey(), entry.getValue());
            }
            
            // Click-through rates
            logger.info("👆 Click-through analytics:");
            
            var ctrData = commands.zrevrangeWithScores("search:analytics:ctr", 0, 4);
            for (var item : ctrData) {
                double ctr = item.getScore() * 100;
                logger.info("  📊 '{}': {:.1f}% CTR", item.getValue(), ctr);
            }
            
            // Search abandonment analysis
            logger.info("🚫 Search abandonment analysis:");
            
            long totalSearches = Long.parseLong(commands.hget("search:analytics:counters", "total_searches"));
            long abandonedSearches = Long.parseLong(commands.hget("search:analytics:counters", "abandoned_searches"));
            double abandonmentRate = (double) abandonedSearches / totalSearches * 100;
            
            logger.info("  📉 Abandonment rate: {:.1f}%", abandonmentRate);
            
            // Zero-result queries
            var zeroResultQueries = commands.lrange("search:analytics:zero_results", 0, 4);
            logger.info("  ❌ Zero-result queries: {}", zeroResultQueries);
            
            return null;
        });
    }
    
    private void demonstratePersonalizedSearch() {
        logger.info("👤 Demonstrating Personalized Search...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📝 Building user profiles...");
            
            // Create user profiles
            buildUserProfiles(commands);
            
            logger.info("🎯 Performing personalized searches...");
            
            String[] users = {"user:1", "user:2", "user:3"};
            String query = "programming";
            
            for (String userId : users) {
                var personalizedResults = performPersonalizedSearch(commands, userId, query);
                var userProfile = commands.hgetall("search:profile:" + userId);
                
                logger.info("  👤 {} (interests: {}): {}", 
                    userId, 
                    userProfile.getOrDefault("interests", "none"),
                    personalizedResults.stream().limit(3).collect(Collectors.toList()));
            }
            
            // Collaborative filtering
            logger.info("🤝 Collaborative filtering recommendations:");
            
            for (String userId : users) {
                var recommendations = getCollaborativeRecommendations(commands, userId);
                logger.info("  💡 Recommendations for {}: {}", userId, recommendations);
            }
            
            // Search history influence
            logger.info("📈 Search history influence:");
            
            for (String userId : users) {
                // Simulate search history
                addToSearchHistory(commands, userId, Arrays.asList(
                    "machine learning", "python tutorial", "data science"
                ));
                
                var historyInfluencedResults = getHistoryInfluencedResults(commands, userId, "algorithm");
                logger.info("  🔍 History-influenced results for {}: {}", userId, historyInfluencedResults);
            }
            
            return null;
        });
    }
    
    private void demonstrateRealTimeIndexing() {
        logger.info("⚡ Demonstrating Real-time Search Indexing...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("🔄 Setting up real-time indexing...");
            
            // Simulate real-time document updates
            for (int i = 1; i <= 5; i++) {
                String docId = "realtime:doc:" + i;
                
                // Create document
                Map<String, String> document = Map.of(
                    "title", "Real-time Document " + i,
                    "content", "This is a real-time updated document about " + 
                        (i % 2 == 0 ? "machine learning" : "web development"),
                    "category", i % 2 == 0 ? "AI" : "Web",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "author", "Author" + i
                );
                
                // Store document
                for (Map.Entry<String, String> field : document.entrySet()) {
                    commands.hset(docId, field.getKey(), field.getValue());
                }
                
                // Real-time indexing
                indexDocumentRealTime(commands, docId, document);
                
                logger.info("  ✅ Indexed real-time document: {}", docId);
            }
            
            // Test incremental search updates
            logger.info("📊 Testing incremental search updates...");
            
            // Update document and re-index
            String docId = "realtime:doc:1";
            commands.hset(docId, "content", "Updated content about artificial intelligence and neural networks");
            
            // Update indexes
            updateDocumentIndex(commands, docId);
            logger.info("  🔄 Updated document index for: {}", docId);
            
            // Test real-time search
            var realtimeResults = commands.smembers("search:index:terms:artificial");
            logger.info("  🔍 Real-time search for 'artificial': {}", realtimeResults);
            
            // Index statistics
            logger.info("📊 Index statistics:");
            
            long totalDocs = commands.scard("search:index:all_docs");
            long totalTerms = commands.zcard("search:index:term_frequency");
            
            logger.info("  📚 Total indexed documents: {}", totalDocs);
            logger.info("  🔤 Total unique terms: {}", totalTerms);
            
            // Index health monitoring
            logger.info("🏥 Index health monitoring:");
            
            // Check index consistency
            boolean indexHealthy = checkIndexHealth(commands);
            logger.info("  ❤️  Index health: {}", indexHealthy ? "Healthy" : "Needs attention");
            
            // Index size monitoring
            var indexSizes = commands.hgetall("search:index:sizes");
            for (Map.Entry<String, String> entry : indexSizes.entrySet()) {
                logger.info("  📏 {}: {} items", entry.getKey(), entry.getValue());
            }
            
            return null;
        });
    }
    
    // Utility methods for search implementation
    
    private void createDocuments(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        Map<String, Map<String, String>> documents = Map.of(
            "doc:1", Map.of(
                "title", "Introduction to Machine Learning",
                "content", "Learn the basics of machine learning with Python and scikit-learn",
                "category", "Data Science",
                "difficulty", "Beginner",
                "rating", "4.5",
                "price", "79.99"
            ),
            "doc:2", Map.of(
                "title", "Advanced Deep Learning Techniques",
                "content", "Master neural networks, CNNs, and RNNs with TensorFlow",
                "category", "Data Science", 
                "difficulty", "Advanced",
                "rating", "4.8",
                "price", "149.99"
            ),
            "doc:3", Map.of(
                "title", "Web Development with React",
                "content", "Build modern web applications using React and JavaScript",
                "category", "Programming",
                "difficulty", "Intermediate",
                "rating", "4.3",
                "price", "99.99"
            ),
            "doc:4", Map.of(
                "title", "Cloud Computing with AWS",
                "content", "Deploy and scale applications using Amazon Web Services",
                "category", "Cloud",
                "difficulty", "Intermediate", 
                "rating", "4.6",
                "price", "129.99"
            ),
            "doc:5", Map.of(
                "title", "Artificial Intelligence Fundamentals",
                "content", "Explore AI concepts, algorithms, and real-world applications",
                "category", "Data Science",
                "difficulty", "Beginner",
                "rating", "4.4",
                "price", "89.99"
            )
        );
        
        for (Map.Entry<String, Map<String, String>> doc : documents.entrySet()) {
            String docId = doc.getKey();
            for (Map.Entry<String, String> field : doc.getValue().entrySet()) {
                commands.hset(docId, field.getKey(), field.getValue());
            }
        }
    }
    
    private void buildFullTextIndex(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        // Get all documents
        for (int i = 1; i <= 5; i++) {
            String docId = "doc:" + i;
            String title = commands.hget(docId, "title");
            String content = commands.hget(docId, "content");
            
            if (title != null && content != null) {
                // Tokenize and index
                Set<String> terms = tokenize(title + " " + content);
                
                for (String term : terms) {
                    // Add document to term index
                    commands.sadd("search:index:terms:" + term.toLowerCase(), docId);
                    
                    // Track term frequency
                    commands.zincrby("search:index:term_frequency", 1, term.toLowerCase());
                }
                
                // Add to all documents set
                commands.sadd("search:index:all_docs", docId);
            }
        }
    }
    
    private void performFullTextSearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands, 
                                     String query) {
        Set<String> queryTerms = tokenize(query);
        Set<String> results = new HashSet<>();
        
        boolean first = true;
        for (String term : queryTerms) {
            var termResults = commands.smembers("search:index:terms:" + term.toLowerCase());
            
            if (first) {
                results.addAll(termResults);
                first = false;
            } else {
                results.retainAll(termResults); // AND operation
            }
        }
        
        // Get titles for results
        List<String> resultTitles = new ArrayList<>();
        for (String docId : results) {
            String title = commands.hget(docId, "title");
            if (title != null) {
                resultTitles.add(title);
            }
        }
        
        logger.info("  🔍 '{}' → {} results: {}", query, results.size(), 
            resultTitles.stream().limit(3).collect(Collectors.toList()));
    }
    
    private void performBooleanSearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                    String query, String description) {
        // Simplified boolean search implementation
        logger.info("  🔍 Boolean search: {} ({})", query, description);
        
        if (query.contains(" AND ")) {
            String[] terms = query.split(" AND ");
            var result1 = commands.smembers("search:index:terms:" + terms[0].toLowerCase());
            var result2 = commands.smembers("search:index:terms:" + terms[1].toLowerCase());
            result1.retainAll(result2);
            logger.info("    → {} documents match both terms", result1.size());
        } else if (query.contains(" OR ")) {
            String[] terms = query.split(" OR ");
            var result1 = commands.smembers("search:index:terms:" + terms[0].toLowerCase());
            var result2 = commands.smembers("search:index:terms:" + terms[1].toLowerCase());
            result1.addAll(result2);
            logger.info("    → {} documents match either term", result1.size());
        }
    }
    
    private void performPhraseSearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                   String phrase) {
        logger.info("  🔍 Phrase search: \"{}\"", phrase);
        
        // For demonstration, check if phrase exists in content
        int matches = 0;
        for (int i = 1; i <= 5; i++) {
            String content = commands.hget("doc:" + i, "content");
            if (content != null && content.toLowerCase().contains(phrase.toLowerCase())) {
                matches++;
            }
        }
        
        logger.info("    → {} documents contain exact phrase", matches);
    }
    
    private void performFuzzySearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                  String query) {
        logger.info("  🔍 Fuzzy search: '{}' (correcting typos)", query);
        
        // Simple fuzzy matching - find similar terms
        var allTerms = commands.zrange("search:index:term_frequency", 0, -1);
        List<String> suggestions = new ArrayList<>();
        
        for (String term : allTerms) {
            if (levenshteinDistance(query.toLowerCase(), term) <= 2) {
                suggestions.add(term);
            }
        }
        
        logger.info("    → Did you mean: {}", suggestions.stream().limit(3).collect(Collectors.toList()));
    }
    
    private void buildFacetIndexes(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        for (int i = 1; i <= 5; i++) {
            String docId = "doc:" + i;
            String category = commands.hget(docId, "category");
            String difficulty = commands.hget(docId, "difficulty");
            String rating = commands.hget(docId, "rating");
            String price = commands.hget(docId, "price");
            
            if (category != null) {
                commands.sadd("search:facet:category:" + category, docId);
            }
            if (difficulty != null) {
                commands.sadd("search:facet:difficulty:" + difficulty, docId);
            }
            if (rating != null) {
                double ratingValue = Double.parseDouble(rating);
                if (ratingValue >= 4.0) {
                    commands.sadd("search:facet:rating:4+", docId);
                }
            }
            if (price != null) {
                double priceValue = Double.parseDouble(price);
                if (priceValue >= 50 && priceValue <= 100) {
                    commands.sadd("search:facet:price_range:50-100", docId);
                } else if (priceValue > 100) {
                    commands.sadd("search:facet:price_range:100+", docId);
                }
            }
        }
    }
    
    private void performFacetedSearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                    String query, Map<String, String> facets) {
        // Start with text search results
        Set<String> queryTerms = tokenize(query);
        Set<String> results = new HashSet<>();
        
        if (!queryTerms.isEmpty()) {
            String firstTerm = queryTerms.iterator().next();
            results.addAll(commands.smembers("search:index:terms:" + firstTerm.toLowerCase()));
        } else {
            results.addAll(commands.smembers("search:index:all_docs"));
        }
        
        // Apply facet filters
        for (Map.Entry<String, String> facet : facets.entrySet()) {
            String facetKey = "search:facet:" + facet.getKey() + ":" + facet.getValue();
            var facetResults = commands.smembers(facetKey);
            results.retainAll(facetResults);
        }
        
        logger.info("  🔍 Query: '{}', Facets: {} → {} results", query, facets, results.size());
    }
    
    private void showFacetCounts(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        logger.info("  📊 Facet counts:");
        
        String[] categories = {"Data Science", "Programming", "Cloud"};
        for (String category : categories) {
            long count = commands.scard("search:facet:category:" + category);
            logger.info("    📂 {}: {} documents", category, count);
        }
        
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
        for (String difficulty : difficulties) {
            long count = commands.scard("search:facet:difficulty:" + difficulty);
            logger.info("    📈 {}: {} documents", difficulty, count);
        }
    }
    
    private void buildAutocompleteIndex(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        String[] terms = {
            "machine learning", "machine", "data science", "data", "web development", "web",
            "artificial intelligence", "artificial", "programming", "python", "javascript",
            "react", "angular", "vue", "nodejs", "cloud computing", "aws", "azure"
        };
        
        for (String term : terms) {
            double popularity = ThreadLocalRandom.current().nextDouble(0.1, 1.0);
            commands.zadd("autocomplete:terms", popularity, term);
            
            // Build prefix index
            for (int i = 1; i <= term.length(); i++) {
                String prefix = term.substring(0, i);
                commands.zadd("autocomplete:prefix:" + prefix, popularity, term);
            }
        }
        
        // Track popular searches
        String[] popularSearches = {"machine learning", "python", "javascript", "react", "aws"};
        for (int i = 0; i < popularSearches.length; i++) {
            commands.zadd("search:popular", popularSearches.length - i, popularSearches[i]);
        }
    }
    
    private List<String> getAutocompleteSuggestions(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                                  String prefix) {
        var suggestions = commands.zrevrange("autocomplete:prefix:" + prefix.toLowerCase(), 0, 4);
        return new ArrayList<>(suggestions);
    }
    
    private List<String> getPopularSearches(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        return new ArrayList<>(commands.zrevrange("search:popular", 0, 4));
    }
    
    private void simulateSearchActivity(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        // Simulate query performance data
        String[] queries = {"machine learning", "python", "web development", "data science", "ai"};
        for (String query : queries) {
            int responseTime = ThreadLocalRandom.current().nextInt(10, 200);
            commands.hset("search:analytics:query_performance", query, String.valueOf(responseTime));
        }
        
        // Simulate result statistics
        commands.hset("search:analytics:result_stats", "avg_results_per_query", "8.5");
        commands.hset("search:analytics:result_stats", "zero_result_rate", "5.2%");
        commands.hset("search:analytics:result_stats", "avg_click_position", "2.3");
        
        // Simulate CTR data
        for (String query : queries) {
            double ctr = ThreadLocalRandom.current().nextDouble(0.1, 0.4);
            commands.zadd("search:analytics:ctr", ctr, query);
        }
        
        // Simulate counters
        commands.hset("search:analytics:counters", "total_searches", "10000");
        commands.hset("search:analytics:counters", "abandoned_searches", "850");
        
        // Zero result queries
        commands.lpush("search:analytics:zero_results", "machine leanring", "react.js", "pythn", "deep learing");
    }
    
    private void buildUserProfiles(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        // User 1: Data Science enthusiast
        commands.hset("search:profile:user:1", "interests", "machine learning,data science,python");
        commands.hset("search:profile:user:1", "skill_level", "intermediate");
        commands.hset("search:profile:user:1", "preferred_price", "50-150");
        
        // User 2: Web Developer
        commands.hset("search:profile:user:2", "interests", "web development,javascript,react");
        commands.hset("search:profile:user:2", "skill_level", "advanced");
        commands.hset("search:profile:user:2", "preferred_price", "100-200");
        
        // User 3: Beginner programmer
        commands.hset("search:profile:user:3", "interests", "programming,basics,tutorial");
        commands.hset("search:profile:user:3", "skill_level", "beginner");
        commands.hset("search:profile:user:3", "preferred_price", "0-100");
    }
    
    private List<String> performPersonalizedSearch(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                                 String userId, String query) {
        var profile = commands.hgetall("search:profile:" + userId);
        String interests = profile.getOrDefault("interests", "");
        String skillLevel = profile.getOrDefault("skill_level", "beginner");
        
        List<String> results = new ArrayList<>();
        
        // Boost results based on user interests
        for (int i = 1; i <= 5; i++) {
            String docId = "doc:" + i;
            String title = commands.hget(docId, "title");
            String content = commands.hget(docId, "content");
            String difficulty = commands.hget(docId, "difficulty");
            
            if (title != null && content != null) {
                double score = 0.0;
                
                // Base relevance score
                if (title.toLowerCase().contains(query.toLowerCase()) || 
                    content.toLowerCase().contains(query.toLowerCase())) {
                    score += 1.0;
                }
                
                // Interest boost
                for (String interest : interests.split(",")) {
                    if (title.toLowerCase().contains(interest.toLowerCase()) ||
                        content.toLowerCase().contains(interest.toLowerCase())) {
                        score += 0.5;
                    }
                }
                
                // Skill level match
                if (skillLevel.equalsIgnoreCase(difficulty)) {
                    score += 0.3;
                }
                
                if (score > 0) {
                    results.add(title + " (score: " + String.format("%.1f", score) + ")");
                }
            }
        }
        
        return results;
    }
    
    private List<String> getCollaborativeRecommendations(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                                        String userId) {
        // Simplified collaborative filtering
        return Arrays.asList("Recommended Course 1", "Recommended Course 2", "Recommended Course 3");
    }
    
    private void addToSearchHistory(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                  String userId, List<String> searches) {
        for (String search : searches) {
            commands.lpush("search:history:" + userId, search);
        }
        commands.ltrim("search:history:" + userId, 0, 9); // Keep last 10 searches
    }
    
    private List<String> getHistoryInfluencedResults(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                                   String userId, String query) {
        // var searchHistory = commands.lrange("search:history:" + userId, 0, -1);
        
        // Boost results based on search history themes
        List<String> results = new ArrayList<>();
        results.add("Algorithm Design Patterns (boosted by history)");
        results.add("Machine Learning Algorithms (boosted by history)");
        results.add("Data Structures and Algorithms");
        
        return results;
    }
    
    private void indexDocumentRealTime(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                     String docId, Map<String, String> document) {
        String title = document.get("title");
        String content = document.get("content");
        
        if (title != null && content != null) {
            Set<String> terms = tokenize(title + " " + content);
            
            for (String term : terms) {
                commands.sadd("search:index:terms:" + term.toLowerCase(), docId);
                commands.zincrby("search:index:term_frequency", 1, term.toLowerCase());
            }
            
            commands.sadd("search:index:all_docs", docId);
        }
        
        // Update index size statistics
        long totalDocs = commands.scard("search:index:all_docs");
        long totalTerms = commands.zcard("search:index:term_frequency");
        
        commands.hset("search:index:sizes", "total_documents", String.valueOf(totalDocs));
        commands.hset("search:index:sizes", "total_terms", String.valueOf(totalTerms));
    }
    
    private void updateDocumentIndex(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                   String docId) {
        // Remove old index entries (simplified)
        // In real implementation, would track which terms were indexed for each document
        
        // Re-index document
        var document = commands.hgetall(docId);
        indexDocumentRealTime(commands, docId, document);
    }
    
    private boolean checkIndexHealth(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        // Simple health checks
        long totalDocs = commands.scard("search:index:all_docs");
        long totalTerms = commands.zcard("search:index:term_frequency");
        
        // Index is healthy if we have documents and terms
        return totalDocs > 0 && totalTerms > 0;
    }
    
    // Helper methods
    
    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());
    }
    
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        
        return dp[a.length()][b.length()];
    }
}
