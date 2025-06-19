package com.example.redis.integration;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests using TestContainers to run Redis in a container
 */
@Testcontainers
class RedisIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--appendonly", "yes");
    
    private RedisConnectionManager connectionManager;
    private RedisConfig config;
    
    @BeforeEach
    void setUp() {
        // Create configuration with TestContainer settings
        System.setProperty("redis.host", redis.getHost());
        System.setProperty("redis.port", String.valueOf(redis.getMappedPort(6379)));
        
        config = new RedisConfig();
        connectionManager = new RedisConnectionManager(config);
    }
    
    @AfterEach
    void tearDown() {
        if (connectionManager != null) {
            connectionManager.close();
        }
        System.clearProperty("redis.host");
        System.clearProperty("redis.port");
    }
    
    @Test
    void shouldConnectToRedisContainer() {
        assertTrue(redis.isRunning());
        assertNotNull(connectionManager);
        
        // Test basic connectivity
        connectionManager.withSyncCommands(commands -> {
            String result = commands.ping();
            assertEquals("PONG", result);
            return null;
        });
    }
    
    @Test
    void shouldPerformBasicCacheOperations() {
        connectionManager.withSyncCommands(commands -> {
            // Test SET/GET
            commands.set("test:key", "test:value");
            String value = commands.get("test:key");
            assertEquals("test:value", value);
            
            // Test TTL
            commands.setex("test:ttl", 60, "expires");
            assertTrue(commands.ttl("test:ttl") > 0);
            
            // Test increment
            commands.incr("test:counter");
            String counter = commands.get("test:counter");
            assertEquals("1", counter);
            
            return null;
        });
    }
    
    @Test
    void shouldPerformStreamOperations() {
        connectionManager.withSyncCommands(commands -> {
            // Add to stream
            String messageId = commands.xadd("test:stream", "field1", "value1", "field2", "value2");
            assertNotNull(messageId);
            
            // Read from stream
            var messages = commands.xrange("test:stream", 
                io.lettuce.core.Range.create("-", "+"));
            assertEquals(1, messages.size());
            assertEquals("value1", messages.get(0).getBody().get("field1"));
            
            return null;
        });
    }
    
    @Test
    void shouldPerformPubSubOperations() throws InterruptedException {
        // Test that we can publish messages (subscriber test would be more complex)
        connectionManager.withSyncCommands(commands -> {
            Long subscribers = commands.publish("test:channel", "test message");
            assertEquals(0L, subscribers); // No subscribers in this test
            return null;
        });
    }
    
    @Test
    void shouldPerformGeospatialOperations() {
        connectionManager.withSyncCommands(commands -> {
            // Add geospatial data
            Long added = commands.geoadd("test:geo", -122.431297, 37.773972, "location1");
            assertEquals(1L, added);
            
            // Query distance
            Double distance = commands.geodist("test:geo", "location1", "location1", 
                io.lettuce.core.GeoArgs.Unit.km);
            assertEquals(0.0, distance, 0.1);
            
            return null;
        });
    }
}
