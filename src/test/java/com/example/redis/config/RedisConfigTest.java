package com.example.redis.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Redis configuration loading
 */
class RedisConfigTest {
    
    @Test
    void shouldLoadConfiguration() {
        RedisConfig config = new RedisConfig();
        
        assertNotNull(config.getHost());
        assertTrue(config.getPort() > 0);
        assertTrue(config.getPoolMaxTotal() > 0);
        assertNotNull(config.getDefaultCacheTtl());
        
        System.out.println("✅ Configuration loaded successfully");
        System.out.println("  Host: " + config.getHost());
        System.out.println("  Port: " + config.getPort());
        System.out.println("  Pool Max: " + config.getPoolMaxTotal());
    }
}
