package com.example.redis.multimodel;

import com.example.redis.config.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Cutting-edge demonstration of Redis as a Multi-Model Database.
 * 
 * Features demonstrated:
 * - JSON document operations (RedisJSON simulation)
 * - Graph-like relationships using Redis data structures
 * - Search and secondary indexing
 * - Document validation and schema enforcement
 * - Complex queries across different data models
 * - ACID transactions across multiple models
 */
public class MultiModelDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiModelDemo.class);
    
    private final RedisConnectionManager connectionManager;
    
    public MultiModelDemo(RedisConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public void demonstrateMultiModel() {
        logger.info("🌐 Starting Multi-Model Database Demonstration");
        
        try {
            // 1. JSON document operations
            demonstrateJSONOperations();
            
            // 2. Graph relationships
            demonstrateGraphOperations();
            
            // 3. Full-text search
            demonstrateSearchOperations();
            
            // 4. Complex queries
            demonstrateComplexQueries();
            
            // 5. ACID transactions
            demonstrateACIDTransactions();
            
            // 6. Schema validation
            demonstrateSchemaValidation();
            
            logger.info("✅ Multi-Model demonstration completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error in multi-model demonstration", e);
        }
    }
    
    private void demonstrateJSONOperations() {
        logger.info("📄 Demonstrating JSON Document Operations...");
        
        connectionManager.withSyncCommands(commands -> {
            // Simulate JSON document storage (using Redis hashes for demonstration)
            logger.info("📋 Creating JSON documents...");
            
            // User documents
            createUserDocument(commands, "user:1", "Alice Smith", "alice@example.com", 
                "Software Engineer", "San Francisco", 85000);
            createUserDocument(commands, "user:2", "Bob Johnson", "bob@example.com", 
                "Product Manager", "New York", 95000);
            createUserDocument(commands, "user:3", "Carol Davis", "carol@example.com", 
                "Data Scientist", "Seattle", 90000);
            
            // Product documents
            createProductDocument(commands, "product:1", "Laptop Pro", "Electronics", 
                1299.99, true, 50);
            createProductDocument(commands, "product:2", "Wireless Mouse", "Electronics", 
                49.99, true, 200);
            createProductDocument(commands, "product:3", "Office Chair", "Furniture", 
                299.99, false, 25);
            
            // Order documents with nested structure
            createOrderDocument(commands, "order:1", "user:1", 
                new String[]{"product:1", "product:2"}, new int[]{1, 2});
            createOrderDocument(commands, "order:2", "user:2", 
                new String[]{"product:3"}, new int[]{1});
            
            logger.info("✅ Created user, product, and order documents");
            
            // Demonstrate document updates
            logger.info("🔧 Updating JSON documents...");
            
            // Update user salary
            commands.hset("user:1", "salary", "87000");
            commands.hset("user:1", "last_updated", LocalDateTime.now().toString());
            
            // Update product stock
            commands.hincrby("product:1", "stock", -1);
            
            // Demonstrate partial document retrieval
            String userName = commands.hget("user:1", "name");
            String userCity = commands.hget("user:1", "city");
            String productPrice = commands.hget("product:1", "price");
            
            logger.info("  👤 User: {} from {}", userName, userCity);
            logger.info("  💰 Product price: ${}", productPrice);
            
            return null;
        });
    }
    
    private void demonstrateGraphOperations() {
        logger.info("🕸️ Demonstrating Graph-like Operations...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("🔗 Creating relationships...");
            
            // User follows relationships
            commands.sadd("user:1:follows", "user:2", "user:3");
            commands.sadd("user:2:follows", "user:3");
            commands.sadd("user:3:follows", "user:1");
            
            // User likes products
            commands.sadd("user:1:likes", "product:1", "product:2");
            commands.sadd("user:2:likes", "product:3");
            commands.sadd("user:3:likes", "product:1", "product:3");
            
            // Product categories (hierarchical relationships)
            commands.sadd("category:electronics:products", "product:1", "product:2");
            commands.sadd("category:furniture:products", "product:3");
            
            // Reverse indexes for efficient queries
            commands.sadd("product:1:liked_by", "user:1", "user:3");
            commands.sadd("product:2:liked_by", "user:1");
            commands.sadd("product:3:liked_by", "user:2", "user:3");
            
            logger.info("✅ Created user-follows, user-likes, and category relationships");
            
            // Demonstrate graph traversals
            logger.info("🔍 Performing graph traversals...");
            
            // Find who user:1 follows
            var user1Follows = commands.smembers("user:1:follows");
            logger.info("  👥 User 1 follows: {}", user1Follows);
            
            // Find mutual followers
            var user1FollowsSet = commands.smembers("user:1:follows");
            var user2FollowsSet = commands.smembers("user:2:follows");
            
            user1FollowsSet.retainAll(user2FollowsSet);
            logger.info("  🤝 User 1 and User 2 both follow: {}", user1FollowsSet);
            
            // Find recommendations (products liked by people you follow)
            logger.info("💡 Finding product recommendations...");
            
            var followedUsers = commands.smembers("user:1:follows");
            for (String followedUser : followedUsers) {
                var likedProducts = commands.smembers(followedUser + ":likes");
                var userOwnLikes = commands.smembers("user:1:likes");
                
                likedProducts.removeAll(userOwnLikes); // Remove already liked products
                
                if (!likedProducts.isEmpty()) {
                    String followerName = commands.hget(followedUser, "name");
                    logger.info("  🎯 Recommended by {}: {}", followerName, likedProducts);
                }
            }
            
            // Demonstrate graph analytics
            logger.info("📊 Computing graph metrics...");
            
            // Calculate user influence (follower count)
            for (int i = 1; i <= 3; i++) {
                String userKey = "user:" + i;
                long followerCount = 0;
                
                // Count how many users follow this user
                for (int j = 1; j <= 3; j++) {
                    if (i != j) {
                        boolean follows = commands.sismember("user:" + j + ":follows", userKey);
                        if (follows) followerCount++;
                    }
                }
                
                String userName = commands.hget(userKey, "name");
                logger.info("  📈 {}: {} followers", userName, followerCount);
            }
            
            return null;
        });
    }
    
    private void demonstrateSearchOperations() {
        logger.info("🔍 Demonstrating Search Operations...");
        
        connectionManager.withSyncCommands(commands -> {
            // Create search indexes (simulated with Redis sets and sorted sets)
            logger.info("📚 Creating search indexes...");
            
            // Text search indexes
            indexUsersByCity(commands);
            indexProductsByCategory(commands);
            indexProductsByPriceRange(commands);
            
            logger.info("✅ Created search indexes");
            
            // Demonstrate search queries
            logger.info("🔎 Performing search queries...");
            
            // Search users by city
            var sfUsers = commands.smembers("index:users:city:san_francisco");
            logger.info("  🌉 Users in San Francisco: {}", sfUsers);
            
            // Search products by category
            var electronicsProducts = commands.smembers("index:products:category:electronics");
            logger.info("  💻 Electronics products: {}", electronicsProducts);
            
            // Range search by price
            var affordableProducts = commands.zrangebyscore("index:products:price", 
                io.lettuce.core.Range.create(0, 100));
            logger.info("  💵 Products under $100: {}", affordableProducts);
            
            var expensiveProducts = commands.zrangebyscore("index:products:price", 
                io.lettuce.core.Range.create(1000, Double.MAX_VALUE));
            logger.info("  💎 Premium products (>$1000): {}", expensiveProducts);
            
            // Compound queries
            logger.info("🎯 Performing compound queries...");
            
            // Find high-earning users in tech cities
            var techCities = commands.sunion("index:users:city:san_francisco", 
                "index:users:city:seattle", "index:users:city:new_york");
            
            for (String userKey : techCities) {
                String salaryStr = commands.hget(userKey, "salary");
                if (salaryStr != null) {
                    int salary = Integer.parseInt(salaryStr);
                    if (salary > 85000) {
                        String name = commands.hget(userKey, "name");
                        String city = commands.hget(userKey, "city");
                        logger.info("  💰 High earner: {} in {} (${:,})", name, city, salary);
                    }
                }
            }
            
            return null;
        });
    }
    
    private void demonstrateComplexQueries() {
        logger.info("🧠 Demonstrating Complex Queries...");
        
        connectionManager.withSyncCommands(commands -> {
            // Complex analytical queries
            logger.info("📊 Running analytical queries...");
            
            // Query 1: Find users who like products in the same category as their purchase history
            logger.info("🎯 Finding users with consistent preferences...");
            
            for (int i = 1; i <= 3; i++) {
                String userKey = "user:" + i;
                String userName = commands.hget(userKey, "name");
                
                var likedProducts = commands.smembers(userKey + ":likes");
                Map<String, Integer> categoryPreferences = new HashMap<>();
                
                for (String productKey : likedProducts) {
                    String category = commands.hget(productKey, "category");
                    categoryPreferences.merge(category, 1, Integer::sum);
                }
                
                if (!categoryPreferences.isEmpty()) {
                    String preferredCategory = categoryPreferences.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("None");
                    
                    logger.info("  👤 {}: prefers {} products", userName, preferredCategory);
                }
            }
            
            // Query 2: Product affinity analysis
            logger.info("🔗 Analyzing product affinities...");
            
            for (String product1 : new String[]{"product:1", "product:2", "product:3"}) {
                var users1 = commands.smembers(product1 + ":liked_by");
                
                for (String product2 : new String[]{"product:1", "product:2", "product:3"}) {
                    if (!product1.equals(product2)) {
                        var users2 = commands.smembers(product2 + ":liked_by");
                        
                        // Find intersection
                        users1.retainAll(users2);
                        
                        if (!users1.isEmpty()) {
                            String p1Name = commands.hget(product1, "name");
                            String p2Name = commands.hget(product2, "name");
                            logger.info("  🎯 {} and {} are liked together by {} users", 
                                p1Name, p2Name, users1.size());
                        }
                        
                        // Reset for next iteration
                        users1 = commands.smembers(product1 + ":liked_by");
                    }
                }
            }
            
            // Query 3: Social influence analysis
            logger.info("👑 Analyzing social influence...");
            
            for (int i = 1; i <= 3; i++) {
                String userKey = "user:" + i;
                String userName = commands.hget(userKey, "name");
                
                var followers = commands.smembers(userKey + ":follows");
                var userLikes = commands.smembers(userKey + ":likes");
                
                int influenceScore = 0;
                for (String followerKey : followers) {
                    var followerLikes = commands.smembers(followerKey + ":likes");
                    
                    // Count overlapping preferences
                    followerLikes.retainAll(userLikes);
                    influenceScore += followerLikes.size();
                }
                
                logger.info("  👑 {}: influence score = {}", userName, influenceScore);
            }
            
            return null;
        });
    }
    
    private void demonstrateACIDTransactions() {
        logger.info("⚛️  Demonstrating ACID Transactions...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("💳 Processing order transaction...");
            
            // Simulate order processing with ACID properties
            String orderKey = "order:3";
            String userKey = "user:1";
            String productKey = "product:1";
            
            try {
                // Start transaction
                commands.multi();
                
                // 1. Create order record
                commands.hset(orderKey, "user_id", userKey);
                commands.hset(orderKey, "product_id", productKey);
                commands.hset(orderKey, "quantity", "1");
                commands.hset(orderKey, "status", "processing");
                commands.hset(orderKey, "timestamp", LocalDateTime.now().toString());
                
                // 2. Update product stock
                commands.hincrby(productKey, "stock", -1);
                
                // 3. Add to user's order history
                commands.sadd(userKey + ":orders", orderKey);
                
                // 4. Update user's spending
                String priceStr = commands.hget(productKey, "price");
                if (priceStr != null) {
                    double price = Double.parseDouble(priceStr);
                    commands.hincrbyfloat(userKey, "total_spent", price);
                }
                
                // Commit transaction
                var results = commands.exec();
                
                if (results != null && !results.wasDiscarded()) {
                    logger.info("✅ Transaction completed successfully");
                    
                    // Verify final state
                    String newStock = commands.hget(productKey, "stock");
                    String orderStatus = commands.hget(orderKey, "status");
                    logger.info("  📦 Product stock: {}", newStock);
                    logger.info("  📋 Order status: {}", orderStatus);
                } else {
                    logger.warn("❌ Transaction was discarded");
                }
                
            } catch (Exception e) {
                logger.error("❌ Transaction failed: {}", e.getMessage());
            }
            
            // Demonstrate rollback scenario
            logger.info("🔄 Demonstrating rollback scenario...");
            
            try {
                commands.multi();
                
                // Try to order more than available stock
                commands.hset("order:4", "user_id", "user:2");
                commands.hset("order:4", "product_id", "product:2");
                commands.hset("order:4", "quantity", "1000"); // More than stock
                
                // Check stock before decrementing
                String currentStock = commands.hget("product:2", "stock");
                int stock = Integer.parseInt(currentStock);
                
                if (stock < 1000) {
                    // Discard transaction
                    commands.discard();
                    logger.info("⚠️  Order rejected: insufficient stock (available: {}, requested: 1000)", stock);
                } else {
                    commands.exec();
                }
                
            } catch (Exception e) {
                logger.error("Transaction error: {}", e.getMessage());
            }
            
            return null;
        });
    }
    
    private void demonstrateSchemaValidation() {
        logger.info("📋 Demonstrating Schema Validation...");
        
        connectionManager.withSyncCommands(commands -> {
            // Define schemas using Redis data structures
            logger.info("📝 Defining schemas...");
            
            // User schema
            commands.hset("schema:user", "name", "string:required");
            commands.hset("schema:user", "email", "email:required");
            commands.hset("schema:user", "age", "integer:optional:min:18:max:100");
            commands.hset("schema:user", "salary", "number:optional:min:0");
            
            // Product schema
            commands.hset("schema:product", "name", "string:required");
            commands.hset("schema:product", "price", "number:required:min:0");
            commands.hset("schema:product", "category", "string:required");
            commands.hset("schema:product", "stock", "integer:required:min:0");
            
            logger.info("✅ Schemas defined");
            
            // Validate documents
            logger.info("🔍 Validating documents...");
            
            // Valid user
            boolean validUser = validateDocument(commands, "user", Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "age", "30",
                "salary", "75000"
            ));
            logger.info("  ✅ Valid user document: {}", validUser);
            
            // Invalid user (missing required field)
            boolean invalidUser1 = validateDocument(commands, "user", Map.of(
                "name", "Jane Doe"
                // Missing email
            ));
            logger.info("  ❌ Invalid user (missing email): {}", invalidUser1);
            
            // Invalid user (age out of range)
            boolean invalidUser2 = validateDocument(commands, "user", Map.of(
                "name", "Old Person",
                "email", "old@example.com",
                "age", "150"
            ));
            logger.info("  ❌ Invalid user (age > 100): {}", invalidUser2);
            
            // Valid product
            boolean validProduct = validateDocument(commands, "product", Map.of(
                "name", "Gaming Mouse",
                "price", "79.99",
                "category", "Electronics",
                "stock", "100"
            ));
            logger.info("  ✅ Valid product document: {}", validProduct);
            
            // Invalid product (negative price)
            boolean invalidProduct = validateDocument(commands, "product", Map.of(
                "name", "Free Item",
                "price", "-10.00",
                "category", "Electronics",
                "stock", "50"
            ));
            logger.info("  ❌ Invalid product (negative price): {}", invalidProduct);
            
            return null;
        });
    }
    
    // Utility methods
    private void createUserDocument(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                  String key, String name, String email, String role, String city, int salary) {
        commands.hset(key, "name", name);
        commands.hset(key, "email", email);
        commands.hset(key, "role", role);
        commands.hset(key, "city", city);
        commands.hset(key, "salary", String.valueOf(salary));
        commands.hset(key, "created_at", LocalDateTime.now().toString());
    }
    
    private void createProductDocument(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                     String key, String name, String category, double price, boolean available, int stock) {
        commands.hset(key, "name", name);
        commands.hset(key, "category", category);
        commands.hset(key, "price", String.valueOf(price));
        commands.hset(key, "available", String.valueOf(available));
        commands.hset(key, "stock", String.valueOf(stock));
        commands.hset(key, "created_at", LocalDateTime.now().toString());
    }
    
    private void createOrderDocument(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                   String key, String userId, String[] productIds, int[] quantities) {
        commands.hset(key, "user_id", userId);
        commands.hset(key, "status", "completed");
        commands.hset(key, "created_at", LocalDateTime.now().toString());
        
        double total = 0.0;
        for (int i = 0; i < productIds.length; i++) {
            String itemKey = key + ":item:" + i;
            commands.hset(itemKey, "product_id", productIds[i]);
            commands.hset(itemKey, "quantity", String.valueOf(quantities[i]));
            
            // Calculate total
            String priceStr = commands.hget(productIds[i], "price");
            if (priceStr != null) {
                total += Double.parseDouble(priceStr) * quantities[i];
            }
        }
        
        commands.hset(key, "total", String.format("%.2f", total));
    }
    
    private void indexUsersByCity(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        for (int i = 1; i <= 3; i++) {
            String userKey = "user:" + i;
            String city = commands.hget(userKey, "city");
            if (city != null) {
                String indexKey = "index:users:city:" + city.toLowerCase().replace(" ", "_");
                commands.sadd(indexKey, userKey);
            }
        }
    }
    
    private void indexProductsByCategory(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        for (int i = 1; i <= 3; i++) {
            String productKey = "product:" + i;
            String category = commands.hget(productKey, "category");
            if (category != null) {
                String indexKey = "index:products:category:" + category.toLowerCase();
                commands.sadd(indexKey, productKey);
            }
        }
    }
    
    private void indexProductsByPriceRange(io.lettuce.core.api.sync.RedisCommands<String, String> commands) {
        for (int i = 1; i <= 3; i++) {
            String productKey = "product:" + i;
            String priceStr = commands.hget(productKey, "price");
            if (priceStr != null) {
                double price = Double.parseDouble(priceStr);
                commands.zadd("index:products:price", price, productKey);
            }
        }
    }
    
    private boolean validateDocument(io.lettuce.core.api.sync.RedisCommands<String, String> commands,
                                   String schemaName, Map<String, String> document) {
        String schemaKey = "schema:" + schemaName;
        var schema = commands.hgetall(schemaKey);
        
        for (Map.Entry<String, String> schemaField : schema.entrySet()) {
            String fieldName = schemaField.getKey();
            String fieldRules = schemaField.getValue();
            String[] rules = fieldRules.split(":");
            
            String fieldValue = document.get(fieldName);
            
            // Check required fields
            if (rules.length > 1 && "required".equals(rules[1]) && fieldValue == null) {
                return false;
            }
            
            // Validate field type and constraints
            if (fieldValue != null) {
                String fieldType = rules[0];
                
                switch (fieldType) {
                    case "integer":
                        try {
                            int value = Integer.parseInt(fieldValue);
                            
                            // Check min/max constraints
                            for (int i = 2; i < rules.length; i += 2) {
                                if ("min".equals(rules[i]) && i + 1 < rules.length) {
                                    int min = Integer.parseInt(rules[i + 1]);
                                    if (value < min) return false;
                                }
                                if ("max".equals(rules[i]) && i + 1 < rules.length) {
                                    int max = Integer.parseInt(rules[i + 1]);
                                    if (value > max) return false;
                                }
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        break;
                        
                    case "number":
                        try {
                            double value = Double.parseDouble(fieldValue);
                            
                            // Check min constraint
                            for (int i = 2; i < rules.length; i += 2) {
                                if ("min".equals(rules[i]) && i + 1 < rules.length) {
                                    double min = Double.parseDouble(rules[i + 1]);
                                    if (value < min) return false;
                                }
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        break;
                        
                    case "email":
                        if (!fieldValue.contains("@") || !fieldValue.contains(".")) {
                            return false;
                        }
                        break;
                }
            }
        }
        
        return true;
    }
}
