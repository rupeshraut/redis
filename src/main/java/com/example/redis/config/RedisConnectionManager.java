package com.example.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection manager for Redis using Lettuce client.
 * Provides connection pooling, health checks, and lifecycle management.
 */
public class RedisConnectionManager implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionManager.class);
    
    private final RedisConfig config;
    private final ClientResources clientResources;
    private final RedisClient redisClient;
    private final GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    public RedisConnectionManager(RedisConfig config) {
        this.config = config;
        this.clientResources = createClientResources();
        this.redisClient = createRedisClient();
        this.connectionPool = createConnectionPool();
        
        logger.info("Redis connection manager initialized");
        config.logConfigSummary();
    }
    
    /**
     * Create optimized client resources
     */
    private ClientResources createClientResources() {
        return DefaultClientResources.builder()
            .ioThreadPoolSize(Runtime.getRuntime().availableProcessors())
            .computationThreadPoolSize(Runtime.getRuntime().availableProcessors())
            .build();
    }
    
    /**
     * Create Redis client with configuration
     */
    private RedisClient createRedisClient() {
        RedisURI redisUri = RedisURI.builder()
            .withHost(config.getHost())
            .withPort(config.getPort())
            .withDatabase(config.getDatabase())
            .withTimeout(config.getCommandTimeout())
            .build();
        
        if (config.getPassword() != null) {
            redisUri.setPassword(config.getPassword());
        }
        
        if (config.isSslEnabled()) {
            redisUri.setSsl(true);
            redisUri.setVerifyPeer(config.isSslVerifyPeer());
        }
        
        RedisClient client = RedisClient.create(clientResources, redisUri);
        client.setDefaultTimeout(config.getCommandTimeout());
        
        logger.info("Redis client created for {}:{}", config.getHost(), config.getPort());
        return client;
    }
    
    /**
     * Create connection pool with optimal settings
     */
    private GenericObjectPool<StatefulRedisConnection<String, String>> createConnectionPool() {
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
            new GenericObjectPoolConfig<>();
        
        poolConfig.setMaxTotal(config.getPoolMaxTotal());
        poolConfig.setMaxIdle(config.getPoolMaxIdle());
        poolConfig.setMinIdle(config.getPoolMinIdle());
        poolConfig.setMaxWait(config.getPoolMaxWait());
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofMinutes(5));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMinutes(1));
        poolConfig.setBlockWhenExhausted(true);
        
        GenericObjectPool<StatefulRedisConnection<String, String>> pool = 
            ConnectionPoolSupport.createGenericObjectPool(
                () -> redisClient.connect(StringCodec.UTF8), 
                poolConfig);
        
        logger.info("Redis connection pool created with max total: {}, max idle: {}", 
            poolConfig.getMaxTotal(), poolConfig.getMaxIdle());
        
        return pool;
    }
    
    /**
     * Get a connection from the pool
     */
    public StatefulRedisConnection<String, String> getConnection() {
        try {
            if (closed.get()) {
                throw new IllegalStateException("Connection manager is closed");
            }
            return connectionPool.borrowObject();
        } catch (Exception e) {
            logger.error("Failed to borrow connection from pool", e);
            throw new RuntimeException("Cannot get Redis connection", e);
        }
    }
    
    /**
     * Return a connection to the pool
     */
    public void returnConnection(StatefulRedisConnection<String, String> connection) {
        if (connection != null && !closed.get()) {
            connectionPool.returnObject(connection);
        }
    }
    
    /**
     * Execute operation with automatic connection management
     */
    public <T> T execute(ConnectionCallback<T> callback) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            return callback.doInConnection(connection);
        } catch (Exception e) {
            logger.error("Error executing Redis operation", e);
            throw new RuntimeException("Redis operation failed", e);
        } finally {
            returnConnection(connection);
        }
    }
    
    /**
     * Get sync commands with automatic connection management
     */
    public <T> T withSyncCommands(SyncCommandCallback<T> callback) {
        return execute(connection -> callback.doWithCommands(connection.sync()));
    }
    
    /**
     * Get async commands with automatic connection management
     */
    public <T> T withAsyncCommands(AsyncCommandCallback<T> callback) {
        return execute(connection -> callback.doWithCommands(connection.async()));
    }
    
    /**
     * Get reactive commands with automatic connection management
     */
    public <T> T withReactiveCommands(ReactiveCommandCallback<T> callback) {
        return execute(connection -> callback.doWithCommands(connection.reactive()));
    }
    
    /**
     * Check if Redis is available
     */
    public boolean isHealthy() {
        try {
            return withSyncCommands(commands -> {
                String result = commands.ping();
                return "PONG".equals(result);
            });
        } catch (Exception e) {
            logger.warn("Redis health check failed", e);
            return false;
        }
    }
    
    /**
     * Get connection pool statistics
     */
    public PoolStats getPoolStats() {
        return new PoolStats(
            connectionPool.getNumActive(),
            connectionPool.getNumIdle(),
            connectionPool.getMaxTotal(),
            connectionPool.getCreatedCount(),
            connectionPool.getBorrowedCount(),
            connectionPool.getReturnedCount()
        );
    }
    
    /**
     * Get Redis server information
     */
    public String getServerInfo() {
        return withSyncCommands(commands -> commands.info("server"));
    }
    
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.info("Closing Redis connection manager");
            
            try {
                connectionPool.close();
                logger.info("Connection pool closed");
            } catch (Exception e) {
                logger.error("Error closing connection pool", e);
            }
            
            try {
                redisClient.shutdown();
                logger.info("Redis client shut down");
            } catch (Exception e) {
                logger.error("Error shutting down Redis client", e);
            }
            
            try {
                clientResources.shutdown();
                logger.info("Client resources shut down");
            } catch (Exception e) {
                logger.error("Error shutting down client resources", e);
            }
        }
    }
    
    /**
     * Get the Redis client for creating specialized connections like pub/sub
     */
    public RedisClient getClient() {
        if (closed.get()) {
            throw new IllegalStateException("Connection manager is closed");
        }
        return redisClient;
    }
    
    // Callback interfaces
    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doInConnection(StatefulRedisConnection<String, String> connection) throws Exception;
    }
    
    @FunctionalInterface
    public interface SyncCommandCallback<T> {
        T doWithCommands(RedisCommands<String, String> commands) throws Exception;
    }
    
    @FunctionalInterface
    public interface AsyncCommandCallback<T> {
        T doWithCommands(RedisAsyncCommands<String, String> commands) throws Exception;
    }
    
    @FunctionalInterface
    public interface ReactiveCommandCallback<T> {
        T doWithCommands(RedisReactiveCommands<String, String> commands) throws Exception;
    }
    
    // Pool statistics record
    public record PoolStats(
        int activeConnections,
        int idleConnections,
        int maxTotal,
        long createdCount,
        long borrowedCount,
        long returnedCount
    ) {
        @Override
        public String toString() {
            return String.format(
                "PoolStats{active=%d, idle=%d, max=%d, created=%d, borrowed=%d, returned=%d}",
                activeConnections, idleConnections, maxTotal, createdCount, borrowedCount, returnedCount
            );
        }
    }
}
