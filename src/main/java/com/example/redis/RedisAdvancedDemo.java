package com.example.redis;

import com.example.redis.ai.VectorSearchDemo;
import com.example.redis.analytics.RealTimeAnalyticsDemo;
import com.example.redis.cache.CacheDemo;
import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import com.example.redis.edge.EdgeComputingDemo;
import com.example.redis.lock.DistributedLockDemo;
import com.example.redis.monitoring.RedisMonitor;
import com.example.redis.multimodel.MultiModelDemo;
import com.example.redis.pubsub.PubSubDemo;
import com.example.redis.ratelimit.RateLimitDemo;
import com.example.redis.search.AdvancedSearchDemo;
import com.example.redis.streams.StreamsDemo;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Main demonstration application showcasing advanced Redis use cases.
 * 
 * This application demonstrates:
 * - Cache-aside, write-through, and write-behind patterns
 * - Pub/Sub messaging and event-driven architecture
 * - Redis Streams for event sourcing and message queues
 * - Distributed locking with Redlock algorithm
 * - Rate limiting and circuit breaker patterns
 * - Performance monitoring and health checks
 */
public class RedisAdvancedDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisAdvancedDemo.class);
    
    private final RedisConfig config;
    private final RedisConnectionManager connectionManager;
    private final RedisMonitor monitor;
    
    public RedisAdvancedDemo() {
        this.config = new RedisConfig();
        this.connectionManager = new RedisConnectionManager(config);
        this.monitor = new RedisMonitor(connectionManager, config);
    }
    
    public static void main(String[] args) {
        RedisAdvancedDemo demo = new RedisAdvancedDemo();
        
        try {
            demo.run();
        } catch (Exception e) {
            logger.error("Demo execution failed", e);
            System.exit(1);
        } finally {
            demo.shutdown();
        }
    }
    
    public void run() throws InterruptedException {
        logger.info("=== Redis Advanced Use Cases Demo ===");
        logger.info("Application: {} v{}", config.getAppName(), config.getAppVersion());
        
        // Check Redis connectivity
        if (!connectionManager.isHealthy()) {
            logger.error("Redis is not available. Please ensure Redis is running on {}:{}", 
                config.getHost(), config.getPort());
            return;
        }
        
        logger.info("✅ Redis connectivity verified");
        
        // Display Redis server information
        displayRedisInfo();
        
        // Start monitoring
        if (config.isMonitoringEnabled()) {
            monitor.start();
            logger.info("✅ Monitoring started");
        }
        
        // Run demonstrations
        runDemonstrations();
        
        // Keep application running to observe monitoring
        logger.info("📊 Monitoring Redis operations for 60 seconds...");
        logger.info("   You can observe metrics and health checks in the logs");
        
        Thread.sleep(TimeUnit.SECONDS.toMillis(60));
        
        logger.info("=== Demo completed successfully ===");
    }
    
    private void runDemonstrations() {
        try {
            // =================== TRADITIONAL REDIS PATTERNS ===================
            
            // 1. Cache Patterns Demo
            logger.info("\n🗄️  --- Cache Patterns Demonstration ---");
            CacheDemo cacheDemo = new CacheDemo(connectionManager, config);
            cacheDemo.demonstrateCachePatterns();
            
            // 2. Pub/Sub Demo
            logger.info("\n📡 --- Pub/Sub Messaging Demonstration ---");
            PubSubDemo pubSubDemo = new PubSubDemo(connectionManager, config);
            pubSubDemo.demonstratePubSub();
            
            // 3. Redis Streams Demo
            logger.info("\n🌊 --- Redis Streams Demonstration ---");
            StreamsDemo streamsDemo = new StreamsDemo(connectionManager, config);
            streamsDemo.demonstrateStreams();
            
            // 4. Distributed Lock Demo
            logger.info("\n🔒 --- Distributed Lock Demonstration ---");
            DistributedLockDemo lockDemo = new DistributedLockDemo(connectionManager, config);
            lockDemo.demonstrateLocking();
            
            // 5. Rate Limiting Demo
            logger.info("\n⏱️  --- Rate Limiting Demonstration ---");
            RateLimitDemo rateLimitDemo = new RateLimitDemo(connectionManager, config);
            rateLimitDemo.demonstrateRateLimit();
            
            // =================== CUTTING-EDGE REDIS FEATURES ===================
            
            // 6. AI/ML Vector Search Demo
            logger.info("\n🤖 --- AI/ML Vector Search Demonstration ---");
            VectorSearchDemo vectorDemo = new VectorSearchDemo(connectionManager, config);
            vectorDemo.demonstrateVectorSearch();
            
            // 7. Real-time Analytics Demo
            logger.info("\n📊 --- Real-time Analytics Demonstration ---");
            RealTimeAnalyticsDemo analyticsDemo = new RealTimeAnalyticsDemo(connectionManager);
            analyticsDemo.demonstrateRealTimeAnalytics();
            
            // 8. Multi-Model Database Demo
            logger.info("\n🌐 --- Multi-Model Database Demonstration ---");
            MultiModelDemo multiModelDemo = new MultiModelDemo(connectionManager);
            multiModelDemo.demonstrateMultiModel();
            
            // 9. Edge Computing Demo
            logger.info("\n🌐 --- Edge Computing Demonstration ---");
            EdgeComputingDemo edgeDemo = new EdgeComputingDemo(connectionManager);
            edgeDemo.demonstrateEdgeComputing();
            
            // 10. Advanced Search Demo
            logger.info("\n🔍 --- Advanced Search Demonstration ---");
            AdvancedSearchDemo searchDemo = new AdvancedSearchDemo(connectionManager);
            searchDemo.demonstrateAdvancedSearch();
            
            // 11. Advanced Operations Demo
            demonstrateAdvancedOperations();
            
            logger.info("\n✅ All cutting-edge demonstrations completed successfully!");
            logger.info("🚀 This showcase demonstrates Redis as a multi-purpose platform for:");
            logger.info("   • Traditional caching and messaging");
            logger.info("   • AI/ML vector operations");
            logger.info("   • Real-time analytics and time series");
            logger.info("   • Multi-model database operations");
            logger.info("   • Edge computing and IoT");
            logger.info("   • Advanced search and discovery");
            
        } catch (Exception e) {
            logger.error("Error during demonstrations", e);
        }
    }
    
    public void shutdown() {
        logger.info("Shutting down Redis Advanced Demo");
        
        try {
            if (monitor != null) {
                monitor.stop();
                logger.info("✅ Monitor stopped");
            }
        } catch (Exception e) {
            logger.error("Error stopping monitor", e);
        }
        
        try {
            if (connectionManager != null) {
                connectionManager.close();
                logger.info("✅ Connection manager closed");
            }
        } catch (Exception e) {
            logger.error("Error closing connection manager", e);
        }
        
        logger.info("👋 Goodbye!");
    }
    
    /**
     * Display Redis server information and statistics
     */
    private void displayRedisInfo() {
        try {
            String serverInfo = connectionManager.getServerInfo();
            logger.info("Redis Server Information:");
            
            // Parse and display key information
            String[] lines = serverInfo.split("\r\n");
            for (String line : lines) {
                if (line.startsWith("redis_version:") || 
                    line.startsWith("redis_mode:") ||
                    line.startsWith("os:") ||
                    line.startsWith("uptime_in_seconds:") ||
                    line.startsWith("connected_clients:") ||
                    line.startsWith("used_memory_human:")) {
                    logger.info("  {}", line);
                }
            }
            
            // Display pool statistics
            var poolStats = connectionManager.getPoolStats();
            logger.info("Connection Pool Statistics: {}", poolStats);
            
        } catch (Exception e) {
            logger.warn("Could not retrieve Redis information", e);
        }
    }
    
    /**
     * Demonstrate advanced Redis operations
     */
    private void demonstrateAdvancedOperations() {
        logger.info("🔧 --- Advanced Operations Demonstration ---");
        
        try {
            // Demonstrate pipeline operations
            try (var connection = connectionManager.getConnection()) {
                var async = connection.async();
                
                var futures = new java.util.ArrayList<io.lettuce.core.RedisFuture<String>>();
                // Batch operations
                for (int i = 0; i < 100; i++) {
                    futures.add(async.set("batch:" + i, "value" + i));
                }
                
                // Wait for all commands to complete
                for (var future : futures) {
                    future.get();
                }
                
                logger.info("✅ Pipeline operations: 100 SET commands batched");
            }
            
            // Demonstrate Lua scripting
            connectionManager.withSyncCommands(commands -> {
                String luaScript = 
                    "local current = redis.call('GET', KEYS[1]) " +
                    "if current == false then " +
                    "    redis.call('SET', KEYS[1], ARGV[1]) " +
                    "    return ARGV[1] " +
                    "else " +
                    "    local new_val = current + ARGV[1] " +
                    "    redis.call('SET', KEYS[1], new_val) " +
                    "    return new_val " +
                    "end";
                
                Object result = commands.eval(luaScript, ScriptOutputType.INTEGER, "counter", "10");
                logger.info("✅ Lua script executed: counter value = {}", result);
                
                return null;
            });
            
            // Demonstrate atomic operations
            connectionManager.withSyncCommands(commands -> {
                commands.multi();
                commands.incr("atomic:counter");
                commands.set("atomic:timestamp", String.valueOf(System.currentTimeMillis()));
                commands.sadd("atomic:set", "item1", "item2", "item3");
                var results = commands.exec();
                logger.info("✅ Atomic transaction executed with {} operations", results.size());
                
                return null;
            });
            
            // Demonstrate hyperloglog for cardinality estimation
            connectionManager.withSyncCommands(commands -> {
                String hllKey = "hll:unique_visitors";
                
                // Add some unique visitors
                commands.pfadd(hllKey, "user1", "user2", "user3", "user1", "user4", "user2");
                Long uniqueCount = commands.pfcount(hllKey);
                logger.info("✅ HyperLogLog estimated unique visitors: {}", uniqueCount);
                
                return null;
            });
            
            // Demonstrate geospatial operations
            connectionManager.withSyncCommands(commands -> {
                String geoKey = "geo:stores";
                
                // Add store locations
                commands.geoadd(geoKey, -122.431297, 37.773972, "store1"); // San Francisco
                commands.geoadd(geoKey, -74.005973, 40.712776, "store2");  // New York
                commands.geoadd(geoKey, -118.243685, 34.052234, "store3"); // Los Angeles
                
                // Find stores near San Francisco (within 1000km)
                var nearbyStores = commands.georadius(geoKey, -122.431297, 37.773972, 1000, 
                    io.lettuce.core.GeoArgs.Unit.km);
                logger.info("✅ Stores within 1000km of San Francisco: {}", nearbyStores);
                
                return null;
            });
            
        } catch (Exception e) {
            logger.error("Error in advanced operations demonstration", e);
        }
    }
}
