package com.example.redis.monitoring;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis monitoring and health check system.
 * Provides metrics collection, health checks, and alerting.
 */
public class RedisMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisMonitor.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    private final MeterRegistry meterRegistry;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // Metrics
    private Counter connectionErrors;
    private Counter operationErrors;
    private Timer operationTimer;
    private final AtomicLong connectedClients = new AtomicLong(0);
    private final AtomicLong usedMemory = new AtomicLong(0);
    private final AtomicLong totalCommands = new AtomicLong(0);
    private final AtomicLong keysCount = new AtomicLong(0);
    
    public RedisMonitor(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
        this.meterRegistry = new SimpleMeterRegistry();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        String prefix = config.getMetricsPrefix();
        
        // Counter metrics
        connectionErrors = Counter.builder(prefix + ".connection.errors")
            .description("Number of Redis connection errors")
            .register(meterRegistry);
            
        operationErrors = Counter.builder(prefix + ".operation.errors")
            .description("Number of Redis operation errors")
            .register(meterRegistry);
        
        // Timer metrics
        operationTimer = Timer.builder(prefix + ".operation.duration")
            .description("Redis operation execution time")
            .register(meterRegistry);
        
        // Gauge metrics
        Gauge.builder(prefix + ".connections.active", connectedClients, AtomicLong::get)
            .description("Number of connected clients")
            .register(meterRegistry);
            
        Gauge.builder(prefix + ".memory.used", usedMemory, AtomicLong::get)
            .description("Used memory in bytes")
            .register(meterRegistry);
            
        Gauge.builder(prefix + ".commands.total", totalCommands, AtomicLong::get)
            .description("Total commands processed")
            .register(meterRegistry);
            
        Gauge.builder(prefix + ".keys.count", keysCount, AtomicLong::get)
            .description("Total number of keys")
            .register(meterRegistry);
        
        logger.info("Metrics initialized with prefix: {}", prefix);
    }
    
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Schedule health checks
            scheduler.scheduleAtFixedRate(
                this::performHealthCheck, 
                0, 
                config.getHealthCheckInterval().getSeconds(), 
                TimeUnit.SECONDS
            );
            
            // Schedule metrics collection
            scheduler.scheduleAtFixedRate(
                this::collectMetrics, 
                10, 
                30, 
                TimeUnit.SECONDS
            );
            
            logger.info("Redis monitor started");
        }
    }
    
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Redis monitor stopped");
        }
    }
    
    private void performHealthCheck() {
        try {
            long startTime = System.currentTimeMillis();
            
            boolean isHealthy = connectionManager.isHealthy();
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (isHealthy) {
                logger.debug("✅ Redis health check passed ({}ms)", responseTime);
            } else {
                logger.warn("❌ Redis health check failed ({}ms)", responseTime);
                connectionErrors.increment();
            }
            
            // Record response time
            operationTimer.record(responseTime, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            logger.error("Health check error", e);
            connectionErrors.increment();
        }
    }
    
    private void collectMetrics() {
        try {
            String info = connectionManager.getServerInfo();
            parseRedisInfo(info);
            
            // Collect pool statistics
            var poolStats = connectionManager.getPoolStats();
            logger.debug("Pool stats: {}", poolStats);
            
            // Log metrics summary
            logMetricsSummary();
            
        } catch (Exception e) {
            logger.error("Error collecting metrics", e);
            operationErrors.increment();
        }
    }
    
    private void parseRedisInfo(String info) {
        String[] lines = info.split("\r\n");
        
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                String key = parts[0];
                String value = parts[1];
                
                switch (key) {
                    case "connected_clients":
                        connectedClients.set(Long.parseLong(value));
                        break;
                    case "used_memory":
                        usedMemory.set(Long.parseLong(value));
                        break;
                    case "total_commands_processed":
                        totalCommands.set(Long.parseLong(value));
                        break;
                }
            }
        }
        
        // Count total keys
        countTotalKeys();
    }
    
    private void countTotalKeys() {
        try {
            connectionManager.withSyncCommands(commands -> {
                // Get database info to count keys
                String keyspaceInfo = commands.info("keyspace");
                
                long totalKeys = 0;
                String[] lines = keyspaceInfo.split("\r\n");
                
                for (String line : lines) {
                    if (line.startsWith("db")) {
                        // Parse line like "db0:keys=1234,expires=100,avg_ttl=3600"
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            String[] keyValuePairs = parts[1].split(",");
                            for (String pair : keyValuePairs) {
                                if (pair.startsWith("keys=")) {
                                    totalKeys += Long.parseLong(pair.substring(5));
                                }
                            }
                        }
                    }
                }
                
                keysCount.set(totalKeys);
                return null;
            });
            
        } catch (Exception e) {
            logger.debug("Error counting keys", e);
        }
    }
    
    private void logMetricsSummary() {
        logger.info("📊 Redis Metrics Summary:");
        logger.info("  Connected Clients: {}", connectedClients.get());
        logger.info("  Used Memory: {} bytes", usedMemory.get());
        logger.info("  Total Commands: {}", totalCommands.get());
        logger.info("  Total Keys: {}", keysCount.get());
        logger.info("  Connection Errors: {}", connectionErrors.count());
        logger.info("  Operation Errors: {}", operationErrors.count());
        
        // Pool statistics
        var poolStats = connectionManager.getPoolStats();
        logger.info("  Pool Active: {}", poolStats.activeConnections());
        logger.info("  Pool Idle: {}", poolStats.idleConnections());
    }
    
    /**
     * Record an operation timing
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Record operation error
     */
    public void recordOperationError() {
        operationErrors.increment();
    }
    
    /**
     * Record connection error
     */
    public void recordConnectionError() {
        connectionErrors.increment();
    }
    
    /**
     * Get current metrics
     */
    public RedisMetrics getCurrentMetrics() {
        return new RedisMetrics(
            connectedClients.get(),
            usedMemory.get(),
            totalCommands.get(),
            keysCount.get(),
            connectionErrors.count(),
            operationErrors.count(),
            connectionManager.getPoolStats()
        );
    }
    
    /**
     * Check if Redis is currently healthy
     */
    public boolean isHealthy() {
        return connectionManager.isHealthy();
    }
    
    /**
     * Get comprehensive health status
     */
    public HealthStatus getHealthStatus() {
        try {
            boolean isConnected = connectionManager.isHealthy();
            var poolStats = connectionManager.getPoolStats();
            
            // Check for concerning metrics
            boolean hasErrors = connectionErrors.count() > 10;
            boolean poolExhausted = poolStats.activeConnections() >= poolStats.maxTotal() * 0.9;
            
            HealthStatus.Status status;
            String message;
            
            if (!isConnected) {
                status = HealthStatus.Status.DOWN;
                message = "Redis connection failed";
            } else if (hasErrors) {
                status = HealthStatus.Status.DEGRADED;
                message = "High error rate detected";
            } else if (poolExhausted) {
                status = HealthStatus.Status.DEGRADED;
                message = "Connection pool nearly exhausted";
            } else {
                status = HealthStatus.Status.UP;
                message = "Redis is healthy";
            }
            
            return new HealthStatus(status, message, getCurrentMetrics());
            
        } catch (Exception e) {
            return new HealthStatus(
                HealthStatus.Status.DOWN, 
                "Health check failed: " + e.getMessage(), 
                null
            );
        }
    }
    
    // Data classes
    public record RedisMetrics(
        long connectedClients,
        long usedMemory,
        long totalCommands,
        long keysCount,
        double connectionErrors,
        double operationErrors,
        RedisConnectionManager.PoolStats poolStats
    ) {}
    
    public record HealthStatus(
        Status status,
        String message,
        RedisMetrics metrics
    ) {
        public enum Status {
            UP, DEGRADED, DOWN
        }
        
        public boolean isHealthy() {
            return status == Status.UP;
        }
        
        public boolean isDegraded() {
            return status == Status.DEGRADED;
        }
        
        public boolean isDown() {
            return status == Status.DOWN;
        }
    }
}
