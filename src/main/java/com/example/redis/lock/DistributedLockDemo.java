package com.example.redis.lock;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import io.lettuce.core.SetArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributed Lock demonstration
 */
public class DistributedLockDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockDemo.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    
    public DistributedLockDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
    }
    
    public void demonstrateLocking() {
        logger.info("🔒 Distributed Lock Demo - Acquiring and releasing locks");
        
        String lockKey = config.getResourceLockPrefix() + "critical_resource";
        String clientId = "client_" + System.currentTimeMillis();
        
        connectionManager.withSyncCommands(commands -> {
            // Acquire lock
            String result = commands.set(lockKey, clientId, SetArgs.Builder.nx().ex(30));
            
            if ("OK".equals(result)) {
                logger.info("✅ Lock acquired for resource by {}", clientId);
                
                // Simulate work
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Release lock
                commands.del(lockKey);
                logger.info("✅ Lock released for resource by {}", clientId);
            } else {
                logger.info("❌ Failed to acquire lock for resource");
            }
            
            return null;
        });
    }
}
