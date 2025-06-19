package com.example.redis.ai;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cutting-edge demonstration of Redis as a Vector Database for AI/ML applications.
 * 
 * Features demonstrated:
 * - Vector similarity search using HNSW algorithm
 * - Semantic search with embeddings
 * - Recommendation systems
 * - Image/text similarity matching
 * - Real-time ML inference caching
 */
public class VectorSearchDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorSearchDemo.class);
    
    private final RedisConnectionManager connectionManager;
    
    public VectorSearchDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
    }
    
    public void demonstrateVectorSearch() {
        logger.info("🤖 Starting AI/ML Vector Search Demonstration");
        
        try {
            // 1. Create vector index for product recommendations
            createProductVectorIndex();
            
            // 2. Insert product embeddings
            insertProductEmbeddings();
            
            // 3. Perform semantic search
            performSemanticSearch();
            
            // 4. Demonstrate real-time ML inference caching
            demonstrateMLInferenceCache();
            
            // 5. Advanced vector operations
            demonstrateAdvancedVectorOps();
            
            logger.info("✅ Vector Search demonstration completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error in vector search demonstration", e);
        }
    }
    
    private void createProductVectorIndex() {
        logger.info("📊 Creating vector search index for products...");
        
        connectionManager.withSyncCommands(commands -> {
            try {
                // Create vector index using RediSearch with HNSW algorithm
                // Try to create index (ignore if exists)
                try {
                    commands.eval(
                        "return redis.call('FT.CREATE', 'products:idx', " +
                        "'ON', 'HASH', 'PREFIX', '1', 'product:', " +
                        "'SCHEMA', " +
                        "'name', 'TEXT', 'SORTABLE', " +
                        "'category', 'TAG', 'SORTABLE', " +
                        "'price', 'NUMERIC', 'SORTABLE', " +
                        "'description', 'TEXT', " +
                        "'embedding', 'VECTOR', 'HNSW', '6', 'TYPE', 'FLOAT32', 'DIM', '128', 'DISTANCE_METRIC', 'COSINE')",
                        io.lettuce.core.ScriptOutputType.STATUS
                    );
                    logger.info("✅ Vector index created successfully");
                } catch (Exception e) {
                    logger.info("ℹ️  Vector index already exists or RediSearch not available - using basic operations");
                }
                
            } catch (Exception e) {
                logger.warn("Vector indexing requires RediSearch module - demonstrating with basic operations");
            }
            
            return null;
        });
    }
    
    private void insertProductEmbeddings() {
        logger.info("📦 Inserting product embeddings...");
        
        connectionManager.withSyncCommands(commands -> {
            // Sample product data with simulated embeddings
            Map<String, ProductWithEmbedding> products = Map.of(
                "product:1", new ProductWithEmbedding("Wireless Headphones", "electronics", 199.99, 
                    "High-quality noise-canceling wireless headphones", generateRandomEmbedding()),
                "product:2", new ProductWithEmbedding("Running Shoes", "sports", 129.99,
                    "Lightweight running shoes for professional athletes", generateRandomEmbedding()),
                "product:3", new ProductWithEmbedding("Coffee Maker", "kitchen", 89.99,
                    "Automatic drip coffee maker with programmable timer", generateRandomEmbedding()),
                "product:4", new ProductWithEmbedding("Bluetooth Speaker", "electronics", 79.99,
                    "Portable waterproof Bluetooth speaker with bass boost", generateRandomEmbedding()),
                "product:5", new ProductWithEmbedding("Yoga Mat", "sports", 39.99,
                    "Non-slip eco-friendly yoga mat for all fitness levels", generateRandomEmbedding())
            );
            
            for (Map.Entry<String, ProductWithEmbedding> entry : products.entrySet()) {
                String key = entry.getKey();
                ProductWithEmbedding product = entry.getValue();
                
                // Store product data as hash
                Map<String, String> productHash = new HashMap<>();
                productHash.put("name", product.name);
                productHash.put("category", product.category);
                productHash.put("price", String.valueOf(product.price));
                productHash.put("description", product.description);
                productHash.put("embedding", encodeEmbedding(product.embedding));
                
                commands.hset(key, productHash);
                
                // Also store in a separate key for vector operations
                commands.set(key + ":vector", encodeEmbedding(product.embedding));
            }
            
            logger.info("✅ Inserted {} products with embeddings", products.size());
            return null;
        });
    }
    
    private void performSemanticSearch() {
        logger.info("🔍 Performing semantic similarity search...");
        
        connectionManager.withSyncCommands(commands -> {
            // Generate query vector (simulating user search embedding)
            float[] queryVector = generateRandomEmbedding();
            
            logger.info("🎯 Searching for products similar to query vector...");
            
            // Since we're simulating without RediSearch, we'll calculate similarities manually
            Map<String, Double> similarities = new HashMap<>();
            
            for (int i = 1; i <= 5; i++) {
                String vectorKey = "product:" + i + ":vector";
                String embeddingStr = commands.get(vectorKey);
                
                if (embeddingStr != null) {
                    float[] productVector = decodeEmbedding(embeddingStr);
                    double similarity = cosineSimilarity(queryVector, productVector);
                    similarities.put("product:" + i, similarity);
                    
                    // Get product name for display
                    String productName = commands.hget("product:" + i, "name");
                    logger.info("  📊 {} - Similarity: {:.4f}", productName, similarity);
                }
            }
            
            // Find top similar products
            String topProduct = similarities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
            
            if (!topProduct.equals("none")) {
                String topProductName = commands.hget(topProduct, "name");
                logger.info("🏆 Most similar product: {} ({})", topProductName, topProduct);
            }
            
            return null;
        });
    }
    
    private void demonstrateMLInferenceCache() {
        logger.info("🧠 Demonstrating ML inference result caching...");
        
        connectionManager.withSyncCommands(commands -> {
            // Simulate ML model inference results caching
            String modelVersion = "v2.1.0";
            String inputHash = "input_abc123";
            
            // Cache key pattern: ml:model:version:input_hash
            String cacheKey = String.format("ml:inference:%s:%s", modelVersion, inputHash);
            
            // Check if inference result is cached
            String cachedResult = commands.get(cacheKey);
            
            if (cachedResult != null) {
                logger.info("💨 Cache hit! Retrieved inference result: {}", cachedResult);
            } else {
                logger.info("⏳ Cache miss - performing ML inference...");
                
                // Simulate ML inference computation
                try {
                    Thread.sleep(100); // Simulate computation time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Store inference result with TTL
                String inferenceResult = String.format("{\"prediction\": %.4f, \"confidence\": %.4f}", 
                    ThreadLocalRandom.current().nextDouble(0, 1),
                    ThreadLocalRandom.current().nextDouble(0.8, 1.0));
                
                commands.setex(cacheKey, 3600, inferenceResult); // Cache for 1 hour
                logger.info("📁 Cached inference result: {}", inferenceResult);
            }
            
            // Demonstrate batch inference caching
            logger.info("📊 Demonstrating batch inference caching...");
            for (int i = 1; i <= 5; i++) {
                String batchKey = String.format("ml:batch:%s:item_%d", modelVersion, i);
                String batchResult = String.format("{\"item_id\": %d, \"score\": %.3f}", 
                    i, ThreadLocalRandom.current().nextDouble(0, 1));
                
                commands.setex(batchKey, 1800, batchResult); // Cache for 30 minutes
            }
            
            logger.info("✅ Cached 5 batch inference results");
            return null;
        });
    }
    
    private void demonstrateAdvancedVectorOps() {
        logger.info("🔬 Demonstrating advanced vector operations...");
        
        connectionManager.withSyncCommands(commands -> {
            // 1. Vector aggregation for user profiles
            logger.info("👤 Creating user preference vectors...");
            
            Map<String, float[]> userPreferences = Map.of(
                "user:1", generateRandomEmbedding(),
                "user:2", generateRandomEmbedding(),
                "user:3", generateRandomEmbedding()
            );
            
            for (Map.Entry<String, float[]> entry : userPreferences.entrySet()) {
                commands.set(entry.getKey() + ":preferences", encodeEmbedding(entry.getValue()));
            }
            
            // 2. Real-time recommendation scoring
            logger.info("⚡ Computing real-time recommendation scores...");
            
            String targetUser = "user:1";
            String userPrefKey = targetUser + ":preferences";
            String userEmbeddingStr = commands.get(userPrefKey);
            
            if (userEmbeddingStr != null) {
                float[] userVector = decodeEmbedding(userEmbeddingStr);
                
                // Score all products for this user
                for (int i = 1; i <= 5; i++) {
                    String productKey = "product:" + i + ":vector";
                    String productEmbeddingStr = commands.get(productKey);
                    
                    if (productEmbeddingStr != null) {
                        float[] productVector = decodeEmbedding(productEmbeddingStr);
                        double score = cosineSimilarity(userVector, productVector);
                        
                        // Store recommendation score
                        String scoreKey = String.format("rec:%s:product:%d", targetUser, i);
                        commands.setex(scoreKey, 300, String.valueOf(score)); // Cache for 5 minutes
                        
                        String productName = commands.hget("product:" + i, "name");
                        logger.info("  🎯 {} - Recommendation score: {:.4f}", productName, score);
                    }
                }
            }
            
            // 3. Vector clustering simulation
            logger.info("🎪 Simulating vector clustering...");
            
            // Create cluster centroids
            Map<String, float[]> clusterCentroids = Map.of(
                "cluster:electronics", generateRandomEmbedding(),
                "cluster:sports", generateRandomEmbedding(),
                "cluster:kitchen", generateRandomEmbedding()
            );
            
            for (Map.Entry<String, float[]> entry : clusterCentroids.entrySet()) {
                commands.set(entry.getKey(), encodeEmbedding(entry.getValue()));
            }
            
            logger.info("✅ Created {} cluster centroids", clusterCentroids.size());
            
            return null;
        });
    }
    
    // Utility methods
    private float[] generateRandomEmbedding() {
        float[] embedding = new float[128];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = ThreadLocalRandom.current().nextFloat() * 2 - 1; // Range [-1, 1]
        }
        return embedding;
    }
    
    private String encodeEmbedding(float[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", embedding[i]));
        }
        return sb.toString();
    }
    
    private float[] decodeEmbedding(String encoded) {
        String[] parts = encoded.split(",");
        float[] embedding = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Float.parseFloat(parts[i]);
        }
        return embedding;
    }
    
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    // Data classes
    private static class ProductWithEmbedding {
        final String name;
        final String category;
        final double price;
        final String description;
        final float[] embedding;
        
        ProductWithEmbedding(String name, String category, double price, String description, float[] embedding) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.description = description;
            this.embedding = embedding;
        }
    }
}
