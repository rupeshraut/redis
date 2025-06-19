package com.example.redis.analytics;

import com.example.redis.config.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cutting-edge demonstration of Redis for Real-time Analytics and Time Series data.
 * 
 * Features demonstrated:
 * - Redis TimeSeries for high-frequency data
 * - Real-time dashboard metrics
 * - Probabilistic data structures (HyperLogLog, Bloom filters)
 * - Sliding window analytics
 * - High-throughput event processing
 * - Real-time anomaly detection
 */
public class RealTimeAnalyticsDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(RealTimeAnalyticsDemo.class);
    
    private final RedisConnectionManager connectionManager;
    
    public RealTimeAnalyticsDemo(RedisConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public void demonstrateRealTimeAnalytics() {
        logger.info("📊 Starting Real-time Analytics Demonstration");
        
        try {
            // 1. Time series metrics
            demonstrateTimeSeriesMetrics();
            
            // 2. Real-time dashboard
            demonstrateRealTimeDashboard();
            
            // 3. Probabilistic data structures
            demonstrateProbabilisticStructures();
            
            // 4. Sliding window analytics
            demonstrateSlidingWindowAnalytics();
            
            // 5. High-frequency event processing
            demonstrateHighFrequencyEvents();
            
            // 6. Real-time anomaly detection
            demonstrateAnomalyDetection();
            
            logger.info("✅ Real-time Analytics demonstration completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error in real-time analytics demonstration", e);
        }
    }
    
    private void demonstrateTimeSeriesMetrics() {
        logger.info("⏰ Demonstrating Time Series metrics...");
        
        connectionManager.withSyncCommands(commands -> {
            long currentTime = System.currentTimeMillis();
            
            // Simulate IoT sensor data
            String[] sensors = {"temperature", "humidity", "pressure", "cpu_usage", "memory_usage"};
            
            for (String sensor : sensors) {
                String timeseriesKey = "ts:sensor:" + sensor;
                
                // Create time series (simulate RedisTimeSeries commands)
                logger.info("📈 Creating time series for sensor: {}", sensor);
                
                // Add historical data points (last 10 minutes)
                for (int i = 0; i < 60; i++) {
                    long timestamp = currentTime - (60 - i) * 10000; // 10-second intervals
                    double value = generateSensorValue(sensor);
                    
                    // Store as sorted set with timestamp as score
                    commands.zadd(timeseriesKey, timestamp, String.format("%.2f", value));
                }
                
                // Add current real-time data
                double currentValue = generateSensorValue(sensor);
                commands.zadd(timeseriesKey, currentTime, String.format("%.2f", currentValue));
                
                // Set expiration for old data (keep only last hour)
                commands.expire(timeseriesKey, 3600);
                
                logger.info("  ✅ {} sensor: current value = {:.2f}", sensor, currentValue);
            }
            
            // Demonstrate aggregations
            logger.info("📊 Computing time series aggregations...");
            
            for (String sensor : sensors) {
                String timeseriesKey = "ts:sensor:" + sensor;
                
                // Get last 5 minutes of data
                long fiveMinutesAgo = currentTime - 300000;
                var recentData = commands.zrangebyscore(timeseriesKey, 
                    io.lettuce.core.Range.create(fiveMinutesAgo, currentTime));
                
                if (!recentData.isEmpty()) {
                    double sum = recentData.stream()
                        .mapToDouble(Double::parseDouble)
                        .sum();
                    double avg = sum / recentData.size();
                    
                    // Store aggregated metrics
                    String avgKey = "ts:agg:" + sensor + ":5m:avg";
                    commands.setex(avgKey, 300, String.format("%.2f", avg));
                    
                    logger.info("  📊 {} - 5min average: {:.2f}", sensor, avg);
                }
            }
            
            return null;
        });
    }
    
    private void demonstrateRealTimeDashboard() {
        logger.info("📱 Demonstrating Real-time Dashboard metrics...");
        
        connectionManager.withSyncCommands(commands -> {
            // Simulate real-time application metrics
            String metricsPrefix = "dashboard:metrics";
            
            // Active users counter
            String activeUsersKey = metricsPrefix + ":active_users";
            int activeUsers = ThreadLocalRandom.current().nextInt(1000, 5000);
            commands.setex(activeUsersKey, 60, String.valueOf(activeUsers));
            
            // Request rate (requests per minute)
            String requestRateKey = metricsPrefix + ":request_rate";
            int requestRate = ThreadLocalRandom.current().nextInt(10000, 50000);
            commands.setex(requestRateKey, 60, String.valueOf(requestRate));
            
            // Error rate
            String errorRateKey = metricsPrefix + ":error_rate";
            double errorRate = ThreadLocalRandom.current().nextDouble(0.1, 2.0);
            commands.setex(errorRateKey, 60, String.format("%.2f", errorRate));
            
            // Response time percentiles
            String[] percentiles = {"p50", "p95", "p99"};
            double[] responseBaseline = {150.0, 500.0, 1000.0};
            
            for (int i = 0; i < percentiles.length; i++) {
                String responseTimeKey = metricsPrefix + ":response_time:" + percentiles[i];
                double responseTime = responseBaseline[i] + ThreadLocalRandom.current().nextDouble(-50, 100);
                commands.setex(responseTimeKey, 60, String.format("%.1f", responseTime));
                
                logger.info("  ⚡ Response time {}: {:.1f}ms", percentiles[i], responseTime);
            }
            
            // Revenue metrics
            String revenueKey = metricsPrefix + ":revenue:today";
            double revenue = ThreadLocalRandom.current().nextDouble(50000, 100000);
            commands.setex(revenueKey, 86400, String.format("%.2f", revenue));
            
            // Geographic distribution (top countries)
            String geoKey = metricsPrefix + ":geo_distribution";
            String[] countries = {"US", "UK", "DE", "FR", "JP", "AU", "CA", "BR"};
            
            for (String country : countries) {
                int visitors = ThreadLocalRandom.current().nextInt(100, 1000);
                commands.zadd(geoKey, visitors, country);
            }
            
            commands.expire(geoKey, 3600);
            
            // Get top 5 countries
            var topCountries = commands.zrevrange(geoKey, 0, 4);
            logger.info("  🌍 Top countries by visitors: {}", topCountries);
            
            logger.info("📊 Dashboard metrics updated:");
            logger.info("  👥 Active users: {}", activeUsers);
            logger.info("  🚀 Request rate: {}/min", requestRate);
            logger.info("  ❌ Error rate: {:.2f}%", errorRate);
            logger.info("  💰 Revenue today: ${:.2f}", revenue);
            
            return null;
        });
    }
    
    private void demonstrateProbabilisticStructures() {
        logger.info("🎲 Demonstrating Probabilistic Data Structures...");
        
        connectionManager.withSyncCommands(commands -> {
            // 1. HyperLogLog for unique visitor counting
            String hllKey = "analytics:unique_visitors";
            
            logger.info("📊 HyperLogLog - Unique visitor counting:");
            
            // Simulate unique visitor tracking
            for (int i = 0; i < 10000; i++) {
                String userId = "user_" + ThreadLocalRandom.current().nextInt(1, 5000);
                commands.pfadd(hllKey, userId);
            }
            
            Long uniqueVisitors = commands.pfcount(hllKey);
            logger.info("  ✅ Estimated unique visitors: {}", uniqueVisitors);
            
            // 2. Bloom filter simulation using sets for demonstration
            String bloomKey = "analytics:visited_pages";
            
            logger.info("🌸 Bloom Filter - Page visit tracking:");
            
            // Add visited pages
            String[] pages = {"/home", "/about", "/products", "/contact", "/blog", "/pricing"};
            for (String page : pages) {
                commands.sadd(bloomKey, page);
            }
            
            // Check if pages were visited
            String[] testPages = {"/home", "/unknown", "/products", "/secret"};
            for (String page : testPages) {
                boolean visited = commands.sismember(bloomKey, page);
                logger.info("  {} Page '{}': {}", visited ? "✅" : "❌", page, visited ? "visited" : "not visited");
            }
            
            // 3. Count-Min Sketch simulation using hashes
            String cmsKey = "analytics:page_views";
            
            logger.info("📈 Count-Min Sketch - Page view frequency:");
            
            // Simulate page view counting
            for (String page : pages) {
                int views = ThreadLocalRandom.current().nextInt(100, 1000);
                commands.hset(cmsKey, page, String.valueOf(views));
                logger.info("  📄 {}: {} views", page, views);
            }
            
            // Get most viewed pages
            var pageViews = commands.hgetall(cmsKey);
            String mostViewed = pageViews.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
            
            logger.info("  🏆 Most viewed page: {}", mostViewed);
            
            return null;
        });
    }
    
    private void demonstrateSlidingWindowAnalytics() {
        logger.info("🪟 Demonstrating Sliding Window Analytics...");
        
        connectionManager.withSyncCommands(commands -> {
            long currentTime = System.currentTimeMillis();
            
            // 1. Sliding window for API rate limiting
            String rateLimitKey = "analytics:api_calls:sliding";
            
            logger.info("⚡ API calls sliding window (last minute):");
            
            // Add API calls with timestamps
            for (int i = 0; i < 50; i++) {
                long timestamp = currentTime - ThreadLocalRandom.current().nextLong(0, 60000);
                String callId = "call_" + i;
                commands.zadd(rateLimitKey, timestamp, callId);
            }
            
            // Clean old entries (older than 1 minute)
            long oneMinuteAgo = currentTime - 60000;
            commands.zremrangebyscore(rateLimitKey, 
                io.lettuce.core.Range.create(0, oneMinuteAgo));
            
            // Count current calls in window
            Long callsInLastMinute = commands.zcard(rateLimitKey);
            logger.info("  📞 API calls in last minute: {}", callsInLastMinute);
            
            // 2. Sliding window for real-time metrics
            String metricsWindowKey = "analytics:metrics:sliding";
            
            // Add metrics points
            for (int i = 0; i < 20; i++) {
                long timestamp = currentTime - i * 3000; // Every 3 seconds
                double value = 100 + ThreadLocalRandom.current().nextDouble(-20, 20);
                commands.zadd(metricsWindowKey, timestamp, String.format("%.2f", value));
            }
            
            // Get last 30 seconds of data
            long thirtySecondsAgo = currentTime - 30000;
            var recentMetrics = commands.zrangebyscore(metricsWindowKey, 
                io.lettuce.core.Range.create(thirtySecondsAgo, currentTime));
            
            if (!recentMetrics.isEmpty()) {
                double avg = recentMetrics.stream()
                    .mapToDouble(Double::parseDouble)
                    .average()
                    .orElse(0.0);
                
                logger.info("  📊 Average metric in last 30s: {:.2f}", avg);
            }
            
            // 3. Moving averages
            String movingAvgKey = "analytics:moving_avg";
            
            // Calculate 5-point moving average
            for (int i = 0; i < 10; i++) {
                double value = 50 + ThreadLocalRandom.current().nextDouble(-10, 10);
                commands.lpush(movingAvgKey, String.format("%.2f", value));
            }
            
            // Keep only last 5 values
            commands.ltrim(movingAvgKey, 0, 4);
            
            var values = commands.lrange(movingAvgKey, 0, -1);
            double movingAvg = values.stream()
                .mapToDouble(Double::parseDouble)
                .average()
                .orElse(0.0);
            
            logger.info("  📈 5-point moving average: {:.2f}", movingAvg);
            
            return null;
        });
    }
    
    private void demonstrateHighFrequencyEvents() {
        logger.info("⚡ Demonstrating High-frequency Event Processing...");
        
        connectionManager.withSyncCommands(commands -> {
            // Simulate high-frequency trading data
            String eventStreamKey = "analytics:events:high_freq";
            
            logger.info("📈 Processing high-frequency events...");
            
            // Batch process events
            long currentTime = System.currentTimeMillis();
            
            for (int batch = 0; batch < 5; batch++) {
                // Process 100 events per batch
                for (int i = 0; i < 100; i++) {
                    long eventTime = currentTime + i;
                    String eventData = String.format("event_%d_%.2f", 
                        i, ThreadLocalRandom.current().nextDouble(100, 200));
                    
                    commands.zadd(eventStreamKey, eventTime, eventData);
                }
                
                logger.info("  ✅ Processed batch {} (100 events)", batch + 1);
            }
            
            // Aggregate events by time windows
            long windowStart = currentTime;
            long windowEnd = currentTime + 20;
            
            var windowEvents = commands.zrangebyscore(eventStreamKey, 
                io.lettuce.core.Range.create(windowStart, windowEnd));
            logger.info("  📊 Events in 20ms window: {}", windowEvents.size());
            
            // Simulate real-time event counters
            String counterKey = "analytics:event_counters";
            
            String[] eventTypes = {"click", "view", "purchase", "signup", "download"};
            for (String eventType : eventTypes) {
                int count = ThreadLocalRandom.current().nextInt(1000, 5000);
                commands.hincrby(counterKey, eventType, count);
            }
            
            var counters = commands.hgetall(counterKey);
            logger.info("  📊 Event counters:");
            counters.forEach((type, count) -> 
                logger.info("    {} {}: {}", getEventEmoji(type), type, count));
            
            return null;
        });
    }
    
    private void demonstrateAnomalyDetection() {
        logger.info("🚨 Demonstrating Real-time Anomaly Detection...");
        
        connectionManager.withSyncCommands(commands -> {
            // Simulate metric monitoring with anomaly detection
            String metricsKey = "analytics:anomaly:metrics";
            String alertsKey = "analytics:anomaly:alerts";
            
            logger.info("🔍 Monitoring metrics for anomalies...");
            
            // Define normal ranges
            Map<String, Double[]> normalRanges = Map.of(
                "cpu_usage", new Double[]{10.0, 80.0},
                "memory_usage", new Double[]{20.0, 85.0},
                "response_time", new Double[]{50.0, 500.0},
                "error_rate", new Double[]{0.0, 5.0}
            );
            
            long currentTime = System.currentTimeMillis();
            
            for (Map.Entry<String, Double[]> entry : normalRanges.entrySet()) {
                String metric = entry.getKey();
                Double[] range = entry.getValue();
                
                // Generate current value (sometimes anomalous)
                double value;
                boolean isAnomaly = ThreadLocalRandom.current().nextDouble() < 0.3; // 30% chance of anomaly
                
                if (isAnomaly) {
                    // Generate anomalous value
                    value = ThreadLocalRandom.current().nextBoolean() ? 
                        range[0] - 20 : range[1] + 20;
                } else {
                    // Generate normal value
                    value = ThreadLocalRandom.current().nextDouble(range[0], range[1]);
                }
                
                // Store metric value
                commands.hset(metricsKey, metric, String.format("%.2f", value));
                
                // Check for anomaly
                if (value < range[0] || value > range[1]) {
                    String alertId = String.format("alert_%s_%d", metric, currentTime);
                    String alertData = String.format("{\"metric\":\"%s\",\"value\":%.2f,\"threshold\":[%.1f,%.1f],\"timestamp\":%d}", 
                        metric, value, range[0], range[1], currentTime);
                    
                    commands.setex(alertsKey + ":" + alertId, 3600, alertData);
                    logger.info("  🚨 ANOMALY DETECTED: {} = {:.2f} (normal range: {:.1f}-{:.1f})", 
                        metric, value, range[0], range[1]);
                } else {
                    logger.info("  ✅ {} = {:.2f} (normal)", metric, value);
                }
            }
            
            // Simulate threshold-based alerting
            String thresholdKey = "analytics:thresholds";
            commands.hset(thresholdKey, "critical_cpu", "90");
            commands.hset(thresholdKey, "critical_memory", "95");
            commands.hset(thresholdKey, "critical_response_time", "1000");
            
            // Simulate alert escalation
            String escalationKey = "analytics:alert_escalation";
            int alertCount = ThreadLocalRandom.current().nextInt(0, 10);
            
            if (alertCount > 5) {
                commands.setex(escalationKey + ":level", 300, "critical");
                logger.info("  🔥 CRITICAL ALERT LEVEL: {} alerts in window", alertCount);
            } else if (alertCount > 2) {
                commands.setex(escalationKey + ":level", 300, "warning");
                logger.info("  ⚠️  WARNING ALERT LEVEL: {} alerts in window", alertCount);
            } else {
                logger.info("  🟢 Normal alert level: {} alerts in window", alertCount);
            }
            
            return null;
        });
    }
    
    // Utility methods
    private double generateSensorValue(String sensor) {
        switch (sensor) {
            case "temperature":
                return 20 + ThreadLocalRandom.current().nextDouble(-5, 15);
            case "humidity":
                return 45 + ThreadLocalRandom.current().nextDouble(-10, 20);
            case "pressure":
                return 1013 + ThreadLocalRandom.current().nextDouble(-50, 50);
            case "cpu_usage":
                return ThreadLocalRandom.current().nextDouble(10, 90);
            case "memory_usage":
                return ThreadLocalRandom.current().nextDouble(20, 85);
            default:
                return ThreadLocalRandom.current().nextDouble(0, 100);
        }
    }
    
    private String getEventEmoji(String eventType) {
        switch (eventType) {
            case "click": return "👆";
            case "view": return "👁️";
            case "purchase": return "💰";
            case "signup": return "📝";
            case "download": return "⬇️";
            default: return "📊";
        }
    }
}
