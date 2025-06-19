package com.example.redis.pubsub;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Redis Pub/Sub demonstration with subscriber and publisher
 */
public class PubSubDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(PubSubDemo.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    
    public PubSubDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
    }
    
    public void demonstratePubSub() {
        logger.info("🚀 PubSub Demo - Demonstrating subscriber and publisher patterns");
        
        try {
            // Start subscriber in a separate thread
            CountDownLatch subscriberReady = new CountDownLatch(1);
            CountDownLatch messagesReceived = new CountDownLatch(3);
            
            CompletableFuture<Void> subscriberTask = CompletableFuture.runAsync(() -> {
                subscribeToChannels(subscriberReady, messagesReceived);
            });
            
            // Wait for subscriber to be ready
            subscriberReady.await(5, TimeUnit.SECONDS);
            logger.info("✅ Subscriber is ready");
            
            // Give subscriber a moment to establish connection
            Thread.sleep(100);
            
            // Publish messages
            publishMessages();
            
            // Wait for messages to be received
            boolean received = messagesReceived.await(10, TimeUnit.SECONDS);
            if (received) {
                logger.info("✅ All messages received by subscriber");
            } else {
                logger.warn("⚠️ Some messages may not have been received");
            }
            
        } catch (Exception e) {
            logger.error("Error in pub/sub demonstration", e);
        }
    }
    
    private void subscribeToChannels(CountDownLatch subscriberReady, CountDownLatch messagesReceived) {
        try {
            // Create a separate pub/sub connection from the connection manager
            var client = connectionManager.getClient();
            var pubSubConnection = client.connectPubSub();
            
            // Add listener for messages
            pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(String channel, String message) {
                    logger.info("📥 Received message on channel '{}': {}", channel, message);
                    messagesReceived.countDown();
                }
                
                @Override
                public void subscribed(String channel, long count) {
                    logger.info("📡 Subscribed to channel: {} (total subscriptions: {})", channel, count);
                    subscriberReady.countDown();
                }
                
                @Override
                public void unsubscribed(String channel, long count) {
                    logger.info("📡 Unsubscribed from channel: {} (remaining subscriptions: {})", channel, count);
                }
            });
            
            var syncCommands = pubSubConnection.sync();
            
            // Subscribe to channels
            syncCommands.subscribe("notifications:user", "events:order", "alerts:system");
            
            // Keep the connection alive for a bit to receive messages
            Thread.sleep(5000);
            
            // Unsubscribe
            syncCommands.unsubscribe("notifications:user", "events:order", "alerts:system");
            
            pubSubConnection.close();
            
        } catch (Exception e) {
            logger.error("Error in subscriber", e);
        }
    }
    
    private void publishMessages() {
        connectionManager.withSyncCommands(commands -> {
            logger.info("📤 Publishing messages to Redis channels");
            
            // Publish messages to different channels
            Long subscribers1 = commands.publish("notifications:user", "User 'john_doe' logged in from IP 192.168.1.100");
            Long subscribers2 = commands.publish("events:order", "Order #12345 created by user 'jane_smith' - Amount: $299.99");
            Long subscribers3 = commands.publish("alerts:system", "System health check completed - All services operational");
            
            logger.info("✅ Published message to 'notifications:user' - {} subscribers", subscribers1);
            logger.info("✅ Published message to 'events:order' - {} subscribers", subscribers2);
            logger.info("✅ Published message to 'alerts:system' - {} subscribers", subscribers3);
            
            return null;
        });
    }
}
