package com.example.redis.ratelimit;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rate Limiting demonstration using Redis patterns:
 * - Token bucket algorithm
 * - Sliding window counter
 * - Fixed window counter
 */
public class RateLimitDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitDemo.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    
    // Lua script for atomic rate limiting check
    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "    redis.call('EXPIRE', key, window) " +
        "end " +
        "if current > limit then " +
        "    return 0 " +
        "else " +
        "    return 1 " +
        "end";
    
    public RateLimitDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
    }
    
    public void demonstrateRateLimit() {
        logger.info("⏱️ Rate Limit Demo - Testing API rate limiting");
        
        String rateLimitKey = "rate_limit:api:user123";
        int limit = config.getApiRequestsPerMinute();
        
        connectionManager.withSyncCommands(commands -> {
            // Simple rate limiting using INCR and EXPIRE
            long currentCount = commands.incr(rateLimitKey);
            
            if (currentCount == 1) {
                commands.expire(rateLimitKey, 60); // 1 minute window
            }
            
            if (currentCount <= limit) {
                logger.info("✅ Request allowed ({}/{})", currentCount, limit);
            } else {
                logger.info("❌ Request denied - rate limit exceeded ({}/{})", currentCount, limit);
            }
            
            return null;
        });
    }
}
