package com.example.redis.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Configuration manager for Redis Advanced Demo.
 * Loads configuration from application.conf and environment variables.
 */
public class RedisConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    private final Config config;
    
    public RedisConfig() {
        this.config = ConfigFactory.load();
        logger.info("Redis configuration loaded");
    }
    
    // Connection settings
    public String getHost() {
        return config.getString("redis.host");
    }
    
    public int getPort() {
        return config.getInt("redis.port");
    }
    
    public String getPassword() {
        return config.hasPath("redis.password") && !config.getIsNull("redis.password") 
            ? config.getString("redis.password") 
            : null;
    }
    
    public int getDatabase() {
        return config.getInt("redis.database");
    }
    
    // SSL settings
    public boolean isSslEnabled() {
        return config.getBoolean("redis.ssl.enabled");
    }
    
    public boolean isSslVerifyPeer() {
        return config.getBoolean("redis.ssl.verify-peer");
    }
    
    // Pool settings
    public int getPoolMaxTotal() {
        return config.getInt("redis.pool.max-total");
    }
    
    public int getPoolMaxIdle() {
        return config.getInt("redis.pool.max-idle");
    }
    
    public int getPoolMinIdle() {
        return config.getInt("redis.pool.min-idle");
    }
    
    public Duration getPoolMaxWait() {
        return config.getDuration("redis.pool.max-wait");
    }
    
    public boolean isTestOnBorrow() {
        return config.getBoolean("redis.pool.test-on-borrow");
    }
    
    // Timeout settings
    public Duration getConnectTimeout() {
        return config.getDuration("redis.timeout.connect");
    }
    
    public Duration getCommandTimeout() {
        return config.getDuration("redis.timeout.command");
    }
    
    // Cache settings
    public Duration getDefaultCacheTtl() {
        return config.getDuration("redis.cache.default-ttl");
    }
    
    public int getMaxCacheEntries() {
        return config.getInt("redis.cache.max-entries");
    }
    
    public Duration getUserCacheTtl() {
        return config.getDuration("redis.cache.user-cache-ttl");
    }
    
    public Duration getSessionCacheTtl() {
        return config.getDuration("redis.cache.session-cache-ttl");
    }
    
    public Duration getApiCacheTtl() {
        return config.getDuration("redis.cache.api-cache-ttl");
    }
    
    // Pub/Sub settings
    public List<String> getPubSubChannels() {
        return config.getStringList("redis.pubsub.channels");
    }
    
    public Duration getSubscriptionTimeout() {
        return config.getDuration("redis.pubsub.subscription-timeout");
    }
    
    public Duration getReconnectDelay() {
        return config.getDuration("redis.pubsub.reconnect-delay");
    }
    
    // Streams settings
    public String getConsumerGroup() {
        return config.getString("redis.streams.consumer-group");
    }
    
    public String getConsumerName() {
        return config.getString("redis.streams.consumer-name");
    }
    
    public Duration getStreamBlockTime() {
        return config.getDuration("redis.streams.block-time");
    }
    
    public int getMaxPending() {
        return config.getInt("redis.streams.max-pending");
    }
    
    public String getEventsStream() {
        return config.getString("redis.streams.events-stream");
    }
    
    public String getNotificationsStream() {
        return config.getString("redis.streams.notifications-stream");
    }
    
    public String getAnalyticsStream() {
        return config.getString("redis.streams.analytics-stream");
    }
    
    // Lock settings
    public Duration getDefaultLockTimeout() {
        return config.getDuration("redis.lock.default-timeout");
    }
    
    public Duration getLockRetryDelay() {
        return config.getDuration("redis.lock.retry-delay");
    }
    
    public int getMaxLockRetries() {
        return config.getInt("redis.lock.max-retries");
    }
    
    public String getResourceLockPrefix() {
        return config.getString("redis.lock.resource-lock-prefix");
    }
    
    public String getSessionLockPrefix() {
        return config.getString("redis.lock.session-lock-prefix");
    }
    
    public String getJobLockPrefix() {
        return config.getString("redis.lock.job-lock-prefix");
    }
    
    // Rate limit settings
    public int getApiRequestsPerMinute() {
        return config.getInt("redis.rate-limit.api-requests-per-minute");
    }
    
    public int getLoginAttemptsPerHour() {
        return config.getInt("redis.rate-limit.login-attempts-per-hour");
    }
    
    public int getPasswordResetPerDay() {
        return config.getInt("redis.rate-limit.password-reset-per-day");
    }
    
    public Duration getDefaultRateLimitWindow() {
        return config.getDuration("redis.rate-limit.default-window");
    }
    
    public Duration getBurstWindow() {
        return config.getDuration("redis.rate-limit.burst-window");
    }
    
    // Application settings
    public String getAppName() {
        return config.getString("app.name");
    }
    
    public String getAppVersion() {
        return config.getString("app.version");
    }
    
    public int getServerPort() {
        return config.getInt("app.server.port");
    }
    
    public String getServerHost() {
        return config.getString("app.server.host");
    }
    
    // Monitoring settings
    public boolean isMonitoringEnabled() {
        return config.getBoolean("app.monitoring.enabled");
    }
    
    public String getMetricsPrefix() {
        return config.getString("app.monitoring.metrics-prefix");
    }
    
    public Duration getHealthCheckInterval() {
        return config.getDuration("app.monitoring.health-check-interval");
    }
    
    public boolean isPrometheusEnabled() {
        return config.getBoolean("app.monitoring.prometheus.enabled");
    }
    
    public int getPrometheusPort() {
        return config.getInt("app.monitoring.prometheus.port");
    }
    
    // Performance settings
    public boolean isPipelineEnabled() {
        return config.getBoolean("performance.pipeline.enabled");
    }
    
    public int getPipelineBatchSize() {
        return config.getInt("performance.pipeline.batch-size");
    }
    
    public Duration getFlushInterval() {
        return config.getDuration("performance.pipeline.flush-interval");
    }
    
    public boolean isCompressionEnabled() {
        return config.getBoolean("performance.compression.enabled");
    }
    
    public int getCompressionThreshold() {
        return config.getInt("performance.compression.threshold");
    }
    
    public String getCompressionAlgorithm() {
        return config.getString("performance.compression.algorithm");
    }
    
    public String getSerializationFormat() {
        return config.getString("performance.serialization.format");
    }
    
    public boolean isPrettyPrint() {
        return config.getBoolean("performance.serialization.pretty-print");
    }
    
    /**
     * Get the complete Redis URI for connection
     */
    public String getRedisUri() {
        StringBuilder uri = new StringBuilder();
        
        if (isSslEnabled()) {
            uri.append("rediss://");
        } else {
            uri.append("redis://");
        }
        
        if (getPassword() != null) {
            uri.append(":").append(getPassword()).append("@");
        }
        
        uri.append(getHost()).append(":").append(getPort());
        uri.append("/").append(getDatabase());
        
        return uri.toString();
    }
    
    /**
     * Log configuration summary
     */
    public void logConfigSummary() {
        logger.info("Redis Configuration Summary:");
        logger.info("  Host: {}:{}", getHost(), getPort());
        logger.info("  Database: {}", getDatabase());
        logger.info("  SSL Enabled: {}", isSslEnabled());
        logger.info("  Pool Max Total: {}", getPoolMaxTotal());
        logger.info("  Default Cache TTL: {}", getDefaultCacheTtl());
        logger.info("  Consumer Group: {}", getConsumerGroup());
        logger.info("  Monitoring Enabled: {}", isMonitoringEnabled());
    }
}
