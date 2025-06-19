package com.example.redis.streams;

import com.example.redis.config.RedisConfig;
import com.example.redis.config.RedisConnectionManager;
import io.lettuce.core.Consumer;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XGroupCreateArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Redis Streams demonstration with producer and consumer groups
 */
public class StreamsDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamsDemo.class);
    
    private final RedisConnectionManager connectionManager;
    private final RedisConfig config;
    
    public StreamsDemo(RedisConnectionManager connectionManager, RedisConfig config) {
        this.connectionManager = connectionManager;
        this.config = config;
    }
    
    public void demonstrateStreams() {
        logger.info("🌊 Streams Demo - Demonstrating Redis Streams with producer and consumer groups");
        
        try {
            // Create streams and producer messages
            createStreamsAndProduce();
            
            // Create consumer groups
            createConsumerGroups();
            
            // Consume messages
            consumeMessages();
            
        } catch (Exception e) {
            logger.error("Error in streams demonstration", e);
        }
    }
    
    private void createStreamsAndProduce() {
        connectionManager.withSyncCommands(commands -> {
            logger.info("📝 Producing events to Redis streams");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            // Add events to streams
            String eventId1 = commands.xadd(config.getEventsStream(), 
                Map.of(
                    "event", "user_signup",
                    "userId", "12345",
                    "email", "user@example.com",
                    "timestamp", timestamp
                ));
            logger.info("✅ Added user signup event to stream: {}", eventId1);
            
            String eventId2 = commands.xadd(config.getEventsStream(), 
                Map.of(
                    "event", "user_login",
                    "userId", "12345",
                    "ip", "192.168.1.100",
                    "timestamp", timestamp
                ));
            logger.info("✅ Added user login event to stream: {}", eventId2);
            
            String eventId3 = commands.xadd(config.getNotificationsStream(), 
                Map.of(
                    "type", "email",
                    "recipient", "user@example.com",
                    "subject", "Welcome to our platform!",
                    "timestamp", timestamp
                ));
            logger.info("✅ Added email notification to stream: {}", eventId3);
            
            String eventId4 = commands.xadd(config.getAnalyticsStream(), 
                Map.of(
                    "action", "page_view",
                    "userId", "12345",
                    "page", "/dashboard",
                    "userAgent", "Mozilla/5.0...",
                    "timestamp", timestamp
                ));
            logger.info("✅ Added analytics event to stream: {}", eventId4);
            
            return null;
        });
    }
    
    private void createConsumerGroups() {
        connectionManager.withSyncCommands(commands -> {
            logger.info("👥 Creating consumer groups");
            
            try {
                // Create consumer groups (ignore errors if they already exist)
                commands.xgroupCreate(XReadArgs.StreamOffset.from(config.getEventsStream(), "0"), 
                    config.getConsumerGroup(), XGroupCreateArgs.Builder.mkstream());
                logger.info("✅ Created consumer group '{}' for events stream", config.getConsumerGroup());
            } catch (Exception e) {
                logger.debug("Consumer group for events stream may already exist: {}", e.getMessage());
            }
            
            try {
                commands.xgroupCreate(XReadArgs.StreamOffset.from(config.getNotificationsStream(), "0"), 
                    config.getConsumerGroup(), XGroupCreateArgs.Builder.mkstream());
                logger.info("✅ Created consumer group '{}' for notifications stream", config.getConsumerGroup());
            } catch (Exception e) {
                logger.debug("Consumer group for notifications stream may already exist: {}", e.getMessage());
            }
            
            try {
                commands.xgroupCreate(XReadArgs.StreamOffset.from(config.getAnalyticsStream(), "0"), 
                    config.getConsumerGroup(), XGroupCreateArgs.Builder.mkstream());
                logger.info("✅ Created consumer group '{}' for analytics stream", config.getConsumerGroup());
            } catch (Exception e) {
                logger.debug("Consumer group for analytics stream may already exist: {}", e.getMessage());
            }
            
            return null;
        });
    }
    
    private void consumeMessages() {
        connectionManager.withSyncCommands(commands -> {
            logger.info("📖 Consuming messages from streams");
            
            String consumerName = "consumer-" + System.currentTimeMillis();
            
            // Read from events stream
            @SuppressWarnings("unchecked")
            List<StreamMessage<String, String>> eventsMessages = commands.xreadgroup(
                Consumer.from(config.getConsumerGroup(), consumerName),
                XReadArgs.StreamOffset.lastConsumed(config.getEventsStream())
            );
            
            for (StreamMessage<String, String> message : eventsMessages) {
                logger.info("📥 Events stream - ID: {}, Data: {}", message.getId(), message.getBody());
                // Acknowledge the message
                commands.xack(config.getEventsStream(), config.getConsumerGroup(), message.getId());
            }
            
            // Read from notifications stream
            @SuppressWarnings("unchecked")
            List<StreamMessage<String, String>> notificationMessages = commands.xreadgroup(
                Consumer.from(config.getConsumerGroup(), consumerName),
                XReadArgs.StreamOffset.lastConsumed(config.getNotificationsStream())
            );
            
            for (StreamMessage<String, String> message : notificationMessages) {
                logger.info("📥 Notifications stream - ID: {}, Data: {}", message.getId(), message.getBody());
                commands.xack(config.getNotificationsStream(), config.getConsumerGroup(), message.getId());
            }
            
            // Read from analytics stream
            @SuppressWarnings("unchecked")
            List<StreamMessage<String, String>> analyticsMessages = commands.xreadgroup(
                Consumer.from(config.getConsumerGroup(), consumerName),
                XReadArgs.StreamOffset.lastConsumed(config.getAnalyticsStream())
            );
            
            for (StreamMessage<String, String> message : analyticsMessages) {
                logger.info("📥 Analytics stream - ID: {}, Data: {}", message.getId(), message.getBody());
                commands.xack(config.getAnalyticsStream(), config.getConsumerGroup(), message.getId());
            }
            
            logger.info("✅ Processed {} events, {} notifications, {} analytics messages", 
                eventsMessages.size(), notificationMessages.size(), analyticsMessages.size());
            
            return null;
        });
    }
}
