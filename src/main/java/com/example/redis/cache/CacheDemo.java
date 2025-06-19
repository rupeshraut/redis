package com.example.redis.cache;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.RedisFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive demonstration of Redis caching patterns:
 * - Cache-aside (lazy loading)
 * - Write-through caching
 * - Write-behind (write-back) caching
 * - Cache warming strategies
 * - TTL management and eviction policies
 */
public class CacheDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheDemo.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final CacheMetrics metrics;
    
    public CacheDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.userRepository = new UserRepository();
        this.metrics = new CacheMetrics();
    }
    
    public void demonstrateCachePatterns() {
        logger.info("Starting cache patterns demonstration");
        
        // 1. Cache-Aside Pattern
        demonstrateCacheAside();
        
        // 2. Write-Through Pattern
        demonstrateWriteThrough();
        
        // 3. Write-Behind Pattern
        demonstrateWriteBehind();
        
        // 4. Cache Warming
        demonstrateCacheWarming();
        
        // 5. TTL and Eviction
        demonstrateTtlManagement();
        
        // 6. Cache Statistics
        displayCacheStatistics();
        
        logger.info("Cache patterns demonstration completed");
    }
    
    /**
     * Cache-Aside Pattern (Lazy Loading)
     * Application manages the cache manually
     */
    private void demonstrateCacheAside() {
        logger.info("🔄 Demonstrating Cache-Aside Pattern");
        
        String userId = "user:123";
        String cacheKey = "cache:user:" + userId;
        
        // First request - cache miss
        User user = getCacheAsideUser(userId);
        logger.info("✅ First request (cache miss): {}", user.name());
        
        // Second request - cache hit
        user = getCacheAsideUser(userId);
        logger.info("✅ Second request (cache hit): {}", user.name());
        
        // Update user and invalidate cache
        updateUserAndInvalidateCache(userId, "Updated Name");
        
        // Third request - cache miss after invalidation
        user = getCacheAsideUser(userId);
        logger.info("✅ Third request (cache miss after invalidation): {}", user.name());
        
        metrics.incrementCacheAsideOperations();
    }
    
    private User getCacheAsideUser(String userId) {
        String cacheKey = "cache:user:" + userId;
        
        return connectionManager.withSyncCommands(commands -> {
            try {
                // Try to get from cache first
                String cachedData = commands.get(cacheKey);
                
                if (cachedData != null) {
                    metrics.incrementCacheHits();
                    logger.debug("Cache HIT for key: {}", cacheKey);
                    return objectMapper.readValue(cachedData, User.class);
                }
                
                // Cache miss - load from database
                metrics.incrementCacheMisses();
                logger.debug("Cache MISS for key: {}", cacheKey);
                
                User user = userRepository.findById(userId);
                
                // Store in cache with TTL
                String userData = objectMapper.writeValueAsString(user);
                commands.setex(cacheKey, config.getUserCacheTtl().getSeconds(), userData);
                
                return user;
                
            } catch (Exception e) {
                logger.error("Error in cache-aside operation", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    private void updateUserAndInvalidateCache(String userId, String newName) {
        String cacheKey = "cache:user:" + userId;
        
        connectionManager.withSyncCommands(commands -> {
            try {
                // Update in database
                userRepository.updateUser(userId, newName);
                
                // Invalidate cache
                commands.del(cacheKey);
                logger.debug("Cache invalidated for key: {}", cacheKey);
                
                return null;
            } catch (Exception e) {
                logger.error("Error updating user and invalidating cache", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Write-Through Pattern
     * Cache is updated synchronously with the database
     */
    private void demonstrateWriteThrough() {
        logger.info("📝 Demonstrating Write-Through Pattern");
        
        String userId = "user:456";
        User user = new User(userId, "WriteThrough User", "user@example.com", LocalDateTime.now());
        
        // Write-through: update both cache and database
        writeThrough(userId, user);
        logger.info("✅ Write-through completed for user: {}", user.name());
        
        // Read from cache
        User cachedUser = readFromCache(userId);
        logger.info("✅ Read from cache: {}", cachedUser != null ? cachedUser.name() : "null");
        
        metrics.incrementWriteThroughOperations();
    }
    
    private void writeThrough(String userId, User user) {
        String cacheKey = "cache:user:" + userId;
        
        connectionManager.withSyncCommands(commands -> {
            try {
                // Start transaction
                commands.multi();
                
                // Update cache
                String userData = objectMapper.writeValueAsString(user);
                commands.setex(cacheKey, config.getUserCacheTtl().getSeconds(), userData);
                
                // Execute transaction
                commands.exec();
                
                // Update database (in real scenario, this would be in the same transaction)
                userRepository.saveUser(user);
                
                logger.debug("Write-through completed for key: {}", cacheKey);
                return null;
                
            } catch (Exception e) {
                logger.error("Error in write-through operation", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Write-Behind Pattern (Write-Back)
     * Cache is updated immediately, database update is deferred
     */
    private void demonstrateWriteBehind() {
        logger.info("⏰ Demonstrating Write-Behind Pattern");
        
        String userId = "user:789";
        User user = new User(userId, "WriteBehind User", "writebehind@example.com", LocalDateTime.now());
        
        // Write-behind: update cache immediately, defer database update
        writeBehind(userId, user);
        logger.info("✅ Write-behind initiated for user: {}", user.name());
        
        // Simulate processing the write-behind queue
        processWriteBehindQueue();
        
        metrics.incrementWriteBehindOperations();
    }
    
    private void writeBehind(String userId, User user) {
        String cacheKey = "cache:user:" + userId;
        String queueKey = "write-behind:queue";
        
        connectionManager.withSyncCommands(commands -> {
            try {
                // Update cache immediately
                String userData = objectMapper.writeValueAsString(user);
                commands.setex(cacheKey, config.getUserCacheTtl().getSeconds(), userData);
                
                // Add to write-behind queue for deferred database update
                String queueItem = objectMapper.writeValueAsString(
                    new WriteBehindItem(userId, userData, System.currentTimeMillis())
                );
                commands.lpush(queueKey, queueItem);
                
                logger.debug("Write-behind queued for key: {}", cacheKey);
                return null;
                
            } catch (Exception e) {
                logger.error("Error in write-behind operation", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    private void processWriteBehindQueue() {
        String queueKey = "write-behind:queue";
        
        connectionManager.withSyncCommands(commands -> {
            try {
                // Process items from the queue (in real scenario, this would run in background)
                String queueItem = commands.rpop(queueKey);
                
                if (queueItem != null) {
                    WriteBehindItem item = objectMapper.readValue(queueItem, WriteBehindItem.class);
                    User user = objectMapper.readValue(item.userData(), User.class);
                    
                    // Simulate database update delay
                    Thread.sleep(100);
                    userRepository.saveUser(user);
                    
                    logger.debug("Write-behind processed for user: {}", item.userId());
                }
                
                return null;
                
            } catch (Exception e) {
                logger.error("Error processing write-behind queue", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Cache Warming Strategies
     */
    private void demonstrateCacheWarming() {
        logger.info("🔥 Demonstrating Cache Warming");
        
        // Warm cache with frequently accessed users
        List<String> frequentUsers = List.of("user:1001", "user:1002", "user:1003");
        
        CompletableFuture<Void> warmingFuture = CompletableFuture.runAsync(() -> {
            warmCache(frequentUsers);
        });
        
        try {
            warmingFuture.get(); // Wait for warming to complete
            logger.info("✅ Cache warming completed for {} users", frequentUsers.size());
        } catch (Exception e) {
            logger.error("Error during cache warming", e);
        }
        
        metrics.incrementCacheWarmingOperations();
    }
    
    private void warmCache(List<String> userIds) {
        try (var connection = connectionManager.getConnection()) {
            var async = connection.async();
            
            var futures = new ArrayList<RedisFuture<String>>();
            for (String userId : userIds) {
                User user = userRepository.findById(userId);
                String cacheKey = "cache:user:" + userId;
                String userData = objectMapper.writeValueAsString(user);
                
                futures.add(async.setex(cacheKey, config.getUserCacheTtl().getSeconds(), userData));
            }
            
            // Wait for all commands to complete
            for (var future : futures) {
                future.get();
            }
            
            logger.debug("Cache warmed for {} users", userIds.size());
            
        } catch (Exception e) {
            logger.error("Error warming cache", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * TTL Management and Eviction Policies
     */
    private void demonstrateTtlManagement() {
        logger.info("⏱️ Demonstrating TTL Management");
        
        String shortTtlKey = "cache:short-ttl:data";
        String longTtlKey = "cache:long-ttl:data";
        
        connectionManager.withSyncCommands(commands -> {
            // Set data with different TTLs
            commands.setex(shortTtlKey, 5, "Short TTL data"); // 5 seconds
            commands.setex(longTtlKey, 3600, "Long TTL data"); // 1 hour
            
            // Check TTLs
            long shortTtl = commands.ttl(shortTtlKey);
            long longTtl = commands.ttl(longTtlKey);
            
            logger.info("✅ Short TTL key expires in {} seconds", shortTtl);
            logger.info("✅ Long TTL key expires in {} seconds", longTtl);
            
            // Extend TTL
            commands.expire(shortTtlKey, 60);
            logger.info("✅ Extended short TTL key to 60 seconds");
            
            return null;
        });
        
        // Demonstrate cache size management
        demonstrateCacheSizeManagement();
        
        metrics.incrementTtlOperations();
    }
    
    private void demonstrateCacheSizeManagement() {
        logger.info("📏 Demonstrating Cache Size Management");
        
        connectionManager.withSyncCommands(commands -> {
            // Set maxmemory policy (in real scenario, this would be in Redis config)
            // commands.configSet("maxmemory-policy", "allkeys-lru");
            
            // Get memory usage
            String memoryInfo = commands.info("memory");
            String[] lines = memoryInfo.split("\r\n");
            
            for (String line : lines) {
                if (line.startsWith("used_memory_human:") || 
                    line.startsWith("maxmemory_human:")) {
                    logger.info("  {}", line);
                }
            }
            
            return null;
        });
    }
    
    private User readFromCache(String userId) {
        String cacheKey = "cache:user:" + userId;
        
        return connectionManager.withSyncCommands(commands -> {
            try {
                String cachedData = commands.get(cacheKey);
                if (cachedData != null) {
                    return objectMapper.readValue(cachedData, User.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Error reading from cache", e);
                return null;
            }
        });
    }
    
    private void displayCacheStatistics() {
        logger.info("📊 Cache Statistics:");
        logger.info("  Cache Hits: {}", metrics.getCacheHits());
        logger.info("  Cache Misses: {}", metrics.getCacheMisses());
        logger.info("  Hit Ratio: {:.2f}%", metrics.getHitRatio() * 100);
        logger.info("  Cache-Aside Operations: {}", metrics.getCacheAsideOperations());
        logger.info("  Write-Through Operations: {}", metrics.getWriteThroughOperations());
        logger.info("  Write-Behind Operations: {}", metrics.getWriteBehindOperations());
        logger.info("  Cache Warming Operations: {}", metrics.getCacheWarmingOperations());
        logger.info("  TTL Operations: {}", metrics.getTtlOperations());
    }
    
    // Supporting classes
    public record User(String id, String name, String email, LocalDateTime createdAt) {}
    
    public record WriteBehindItem(String userId, String userData, long timestamp) {}
    
    /**
     * Simulated user repository
     */
    private static class UserRepository {
        
        public User findById(String userId) {
            // Simulate database lookup delay
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return new User(
                userId, 
                "User " + userId.substring(userId.length() - 3), 
                "user" + userId.substring(userId.length() - 3) + "@example.com",
                LocalDateTime.now()
            );
        }
        
        public void saveUser(User user) {
            // Simulate database save delay
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void updateUser(String userId, String newName) {
            // Simulate database update
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Cache metrics tracking
     */
    private static class CacheMetrics {
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private long cacheAsideOperations = 0;
        private long writeThroughOperations = 0;
        private long writeBehindOperations = 0;
        private long cacheWarmingOperations = 0;
        private long ttlOperations = 0;
        
        public synchronized void incrementCacheHits() { cacheHits++; }
        public synchronized void incrementCacheMisses() { cacheMisses++; }
        public synchronized void incrementCacheAsideOperations() { cacheAsideOperations++; }
        public synchronized void incrementWriteThroughOperations() { writeThroughOperations++; }
        public synchronized void incrementWriteBehindOperations() { writeBehindOperations++; }
        public synchronized void incrementCacheWarmingOperations() { cacheWarmingOperations++; }
        public synchronized void incrementTtlOperations() { ttlOperations++; }
        
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public double getHitRatio() { 
            long total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }
        public long getCacheAsideOperations() { return cacheAsideOperations; }
        public long getWriteThroughOperations() { return writeThroughOperations; }
        public long getWriteBehindOperations() { return writeBehindOperations; }
        public long getCacheWarmingOperations() { return cacheWarmingOperations; }
        public long getTtlOperations() { return ttlOperations; }
    }
}
