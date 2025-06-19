package com.example.redis.edge;

import com.example.redis.config.RedisConnectionManager;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cutting-edge demonstration of Redis for Edge Computing scenarios.
 * 
 * Features demonstrated:
 * - Redis Functions (server-side JavaScript execution)
 * - Edge data processing and aggregation
 * - Real-time decision making at the edge
 * - IoT data collection and filtering
 * - Distributed computing patterns
 * - Edge-to-cloud synchronization
 */
public class EdgeComputingDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeComputingDemo.class);
    
    private final RedisConnectionManager connectionManager;
    
    public EdgeComputingDemo(RedisConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public void demonstrateEdgeComputing() {
        logger.info("🌐 Starting Edge Computing Demonstration");
        
        try {
            // 1. Redis Functions for server-side processing
            demonstrateRedisFunctions();
            
            // 2. IoT data collection and processing
            demonstrateIoTDataProcessing();
            
            // 3. Real-time edge analytics
            demonstrateEdgeAnalytics();
            
            // 4. Edge decision making
            demonstrateEdgeDecisionMaking();
            
            // 5. Edge-to-cloud synchronization
            demonstrateEdgeCloudSync();
            
            // 6. Distributed edge coordination
            demonstrateEdgeCoordination();
            
            logger.info("✅ Edge Computing demonstration completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error in edge computing demonstration", e);
        }
    }
    
    private void demonstrateRedisFunctions() {
        logger.info("⚡ Demonstrating Redis Functions (Server-side Processing)...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📝 Creating Redis Functions...");
            
            // Function 1: IoT sensor data aggregation
            String sensorAggFunction = 
                "local function aggregate_sensor_data(keys, args)\n" +
                "    local sensor_key = keys[1]\n" +
                "    local window_seconds = tonumber(args[1])\n" +
                "    local current_time = redis.call('TIME')[1]\n" +
                "    local window_start = current_time - window_seconds\n" +
                "    \n" +
                "    -- Get sensor readings in time window\n" +
                "    local readings = redis.call('ZRANGEBYSCORE', sensor_key, window_start, current_time)\n" +
                "    \n" +
                "    if #readings == 0 then\n" +
                "        return {count=0, avg=0, min=0, max=0}\n" +
                "    end\n" +
                "    \n" +
                "    local sum = 0\n" +
                "    local min_val = tonumber(readings[1])\n" +
                "    local max_val = tonumber(readings[1])\n" +
                "    \n" +
                "    for i = 1, #readings do\n" +
                "        local val = tonumber(readings[i])\n" +
                "        sum = sum + val\n" +
                "        if val < min_val then min_val = val end\n" +
                "        if val > max_val then max_val = val end\n" +
                "    end\n" +
                "    \n" +
                "    return {count=#readings, avg=sum/#readings, min=min_val, max=max_val}\n" +
                "end\n" +
                "return aggregate_sensor_data(KEYS, ARGV)";
            
            // Function 2: Real-time anomaly detection
            String anomalyDetectionFunction = 
                "local function detect_anomaly(keys, args)\n" +
                "    local metric_key = keys[1]\n" +
                "    local threshold_key = keys[2]\n" +
                "    local current_value = tonumber(args[1])\n" +
                "    \n" +
                "    -- Get threshold values\n" +
                "    local min_threshold = tonumber(redis.call('HGET', threshold_key, 'min') or 0)\n" +
                "    local max_threshold = tonumber(redis.call('HGET', threshold_key, 'max') or 100)\n" +
                "    \n" +
                "    -- Store current value\n" +
                "    local timestamp = redis.call('TIME')[1]\n" +
                "    redis.call('ZADD', metric_key, timestamp, current_value)\n" +
                "    \n" +
                "    -- Check for anomaly\n" +
                "    local is_anomaly = current_value < min_threshold or current_value > max_threshold\n" +
                "    \n" +
                "    if is_anomaly then\n" +
                "        local alert_key = 'alerts:' .. metric_key\n" +
                "        local alert_data = string.format('{\"value\":%f,\"min\":%f,\"max\":%f,\"timestamp\":%s}',\n" +
                "            current_value, min_threshold, max_threshold, timestamp)\n" +
                "        redis.call('LPUSH', alert_key, alert_data)\n" +
                "        redis.call('EXPIRE', alert_key, 3600)\n" +
                "    end\n" +
                "    \n" +
                "    return {value=current_value, anomaly=is_anomaly, min=min_threshold, max=max_threshold}\n" +
                "end\n" +
                "return detect_anomaly(KEYS, ARGV)";
            
            // Since Redis Functions might not be available, we'll demonstrate with Lua scripts
            logger.info("📊 Testing sensor aggregation function...");
            
            // Prepare test data
            String sensorKey = "edge:sensor:temperature";
            long currentTime = System.currentTimeMillis() / 1000;
            
            // Add some sensor readings
            for (int i = 0; i < 10; i++) {
                double temperature = 20 + ThreadLocalRandom.current().nextGaussian() * 5;
                long timestamp = currentTime - (10 - i) * 60; // Every minute
                commands.zadd(sensorKey, timestamp, String.valueOf(temperature));
            }
            
            try {
                // Test the aggregation function
                var result = commands.eval(sensorAggFunction, ScriptOutputType.VALUE, 
                    new String[]{sensorKey}, "600"); // 10 minute window
                
                logger.info("  📈 Sensor aggregation result: {}", result);
            } catch (Exception e) {
                logger.info("  ℹ️  Redis Functions simulation - using basic Lua script");
                
                // Fallback to simpler aggregation
                var readings = commands.zrangebyscore(sensorKey, 
                    io.lettuce.core.Range.create(currentTime - 600, currentTime));
                
                if (!readings.isEmpty()) {
                    double sum = readings.stream().mapToDouble(Double::parseDouble).sum();
                    double avg = sum / readings.size();
                    logger.info("  📊 Average temperature (10min): {:.2f}°C", avg);
                }
            }
            
            logger.info("🚨 Testing anomaly detection function...");
            
            // Set thresholds
            commands.hset("edge:thresholds:temperature", "min", "15");
            commands.hset("edge:thresholds:temperature", "max", "35");
            
            // Test with normal and anomalous values
            double[] testValues = {22.5, 45.0, 10.0, 25.0}; // 45.0 and 10.0 are anomalies
            
            for (double testValue : testValues) {
                try {
                    var result = commands.eval(anomalyDetectionFunction, ScriptOutputType.VALUE,
                        new String[]{"edge:metrics:temperature", "edge:thresholds:temperature"},
                        String.valueOf(testValue));
                    
                    logger.info("  🌡️  Value {:.1f}°C: {}", testValue, result);
                } catch (Exception e) {
                    // Fallback implementation
                    boolean isAnomaly = testValue < 15 || testValue > 35;
                    logger.info("  🌡️  Value {:.1f}°C: anomaly={}", testValue, isAnomaly);
                    
                    if (isAnomaly) {
                        commands.lpush("alerts:edge:metrics:temperature", 
                            String.format("{\"value\":%.1f,\"timestamp\":%d}", testValue, currentTime));
                    }
                }
            }
            
            return null;
        });
    }
    
    private void demonstrateIoTDataProcessing() {
        logger.info("🔧 Demonstrating IoT Data Collection and Processing...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📡 Simulating IoT device data collection...");
            
            // Simulate multiple IoT devices
            String[] deviceTypes = {"temperature", "humidity", "pressure", "motion", "light"};
            String[] locations = {"factory_floor", "warehouse", "office", "outdoor"};
            
            long currentTime = System.currentTimeMillis();
            
            // Collect data from multiple devices
            for (String location : locations) {
                for (String deviceType : deviceTypes) {
                    String deviceId = String.format("device_%s_%s", location, deviceType);
                    String dataKey = "edge:iot:" + deviceId;
                    
                    // Simulate sensor readings
                    double value = generateIoTValue(deviceType);
                    
                    // Store with timestamp
                    commands.zadd(dataKey, currentTime, String.valueOf(value));
                    
                    // Keep only last hour of data
                    long oneHourAgo = currentTime - 3600000;
                    commands.zremrangebyscore(dataKey, 
                        io.lettuce.core.Range.create(0, oneHourAgo));
                    
                    // Store latest value for quick access
                    commands.hset("edge:iot:latest", deviceId, String.valueOf(value));
                    
                    logger.info("  📊 {}: {} = {:.2f}", location, deviceType, value);
                }
            }
            
            logger.info("🔄 Processing edge data aggregation...");
            
            // Aggregate by location
            for (String location : locations) {
                Map<String, Double> locationData = new HashMap<>();
                
                for (String deviceType : deviceTypes) {
                    String deviceId = String.format("device_%s_%s", location, deviceType);
                    String valueStr = commands.hget("edge:iot:latest", deviceId);
                    
                    if (valueStr != null) {
                        locationData.put(deviceType, Double.parseDouble(valueStr));
                    }
                }
                
                // Store aggregated location data
                for (Map.Entry<String, Double> entry : locationData.entrySet()) {
                    commands.hset("edge:location:" + location, entry.getKey(), 
                        String.valueOf(entry.getValue()));
                }
                
                // Calculate location comfort index (example metric)
                double temp = locationData.getOrDefault("temperature", 20.0);
                double humidity = locationData.getOrDefault("humidity", 50.0);
                double comfortIndex = calculateComfortIndex(temp, humidity);
                
                commands.hset("edge:location:" + location, "comfort_index", 
                    String.valueOf(comfortIndex));
                
                logger.info("  🏢 {}: comfort index = {:.1f}", location, comfortIndex);
            }
            
            // Data filtering and compression
            logger.info("🗜️  Applying edge data filtering...");
            
            for (String deviceType : deviceTypes) {
                String filterKey = "edge:filtered:" + deviceType;
                double threshold = getFilterThreshold(deviceType);
                
                int filteredCount = 0;
                for (String location : locations) {
                    String deviceId = String.format("device_%s_%s", location, deviceType);
                    String valueStr = commands.hget("edge:iot:latest", deviceId);
                    
                    if (valueStr != null) {
                        double value = Double.parseDouble(valueStr);
                        
                        // Only send to cloud if value exceeds threshold for change
                        String lastSentKey = "edge:last_sent:" + deviceId;
                        String lastSentStr = commands.get(lastSentKey);
                        
                        boolean shouldSend = false;
                        if (lastSentStr == null) {
                            shouldSend = true;
                        } else {
                            double lastSent = Double.parseDouble(lastSentStr);
                            if (Math.abs(value - lastSent) > threshold) {
                                shouldSend = true;
                            }
                        }
                        
                        if (shouldSend) {
                            commands.lpush(filterKey, String.format("%.2f", value));
                            commands.setex(lastSentKey, 3600, String.valueOf(value));
                            filteredCount++;
                        }
                    }
                }
                
                logger.info("    📤 {}: {} values queued for cloud sync", deviceType, filteredCount);
            }
            
            return null;
        });
    }
    
    private void demonstrateEdgeAnalytics() {
        logger.info("📊 Demonstrating Real-time Edge Analytics...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("⚡ Computing real-time analytics at edge...");
            
            // Moving averages for smooth data
            String[] metrics = {"temperature", "humidity", "pressure"};
            
            for (String metric : metrics) {
                String movingAvgKey = "edge:analytics:moving_avg:" + metric;
                String rawDataKey = "edge:iot:device_factory_floor_" + metric;
                
                // Get latest values
                var recentValues = commands.zrevrange(rawDataKey, 0, 9); // Last 10 values
                
                if (!recentValues.isEmpty()) {
                    double sum = recentValues.stream()
                        .mapToDouble(Double::parseDouble)
                        .sum();
                    double movingAverage = sum / recentValues.size();
                    
                    // Store moving average
                    commands.setex(movingAvgKey, 300, String.format("%.2f", movingAverage));
                    
                    // Calculate trend
                    if (recentValues.size() >= 5) {
                        double firstHalf = recentValues.subList(0, 5).stream()
                            .mapToDouble(Double::parseDouble)
                            .average().orElse(0);
                        double secondHalf = recentValues.subList(5, recentValues.size()).stream()
                            .mapToDouble(Double::parseDouble)
                            .average().orElse(0);
                        
                        String trend = firstHalf > secondHalf ? "decreasing" : 
                                      firstHalf < secondHalf ? "increasing" : "stable";
                        
                        commands.setex("edge:analytics:trend:" + metric, 300, trend);
                        
                        logger.info("  📈 {}: avg={:.2f}, trend={}", metric, movingAverage, trend);
                    }
                }
            }
            
            // Predictive analytics at edge
            logger.info("🔮 Running predictive analytics...");
            
            // Simple linear prediction for temperature
            String tempKey = "edge:iot:device_factory_floor_temperature";
            var tempHistory = commands.zrevrange(tempKey, 0, 19); // Last 20 values
            
            if (tempHistory.size() >= 10) {
                // Simple linear regression prediction
                double prediction = performLinearPrediction(tempHistory);
                commands.setex("edge:prediction:temperature:next_hour", 300, 
                    String.format("%.2f", prediction));
                
                logger.info("  🎯 Temperature prediction (next hour): {:.2f}°C", prediction);
                
                // Predictive alerting
                if (prediction > 35 || prediction < 15) {
                    String alertKey = "edge:predictive_alerts:temperature";
                    String alertData = String.format("{\"predicted_value\":%.2f,\"timestamp\":%d,\"type\":\"temperature_warning\"}", 
                        prediction, System.currentTimeMillis());
                    
                    commands.lpush(alertKey, alertData);
                    commands.expire(alertKey, 3600);
                    
                    logger.info("  🚨 Predictive alert: Temperature may reach {:.2f}°C", prediction);
                }
            }
            
            // Edge pattern detection
            logger.info("🔍 Detecting patterns at edge...");
            
            // Detect equipment usage patterns
            String motionKey = "edge:iot:device_factory_floor_motion";
            var motionData = commands.zrevrange(motionKey, 0, 59); // Last hour
            
            if (!motionData.isEmpty()) {
                long activeMinutes = motionData.stream()
                    .mapToLong(s -> (long) Double.parseDouble(s))
                    .filter(v -> v > 0)
                    .count();
                
                double utilizationRate = (double) activeMinutes / motionData.size() * 100;
                
                commands.setex("edge:analytics:utilization_rate", 300, 
                    String.format("%.1f", utilizationRate));
                
                String utilizationLevel = utilizationRate > 80 ? "high" : 
                                        utilizationRate > 50 ? "medium" : "low";
                
                logger.info("  🏭 Equipment utilization: {:.1f}% ({})", utilizationRate, utilizationLevel);
            }
            
            return null;
        });
    }
    
    private void demonstrateEdgeDecisionMaking() {
        logger.info("🧠 Demonstrating Real-time Edge Decision Making...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("⚡ Making autonomous edge decisions...");
            
            // Decision 1: Automatic cooling system control
            String tempStr = commands.hget("edge:location:factory_floor", "temperature");
            String humidityStr = commands.hget("edge:location:factory_floor", "humidity");
            
            if (tempStr != null && humidityStr != null) {
                double temperature = Double.parseDouble(tempStr);
                double humidity = Double.parseDouble(humidityStr);
                
                // Decision logic
                boolean shouldActivateCooling = temperature > 28 || humidity > 70;
                boolean shouldActivateDehumidifier = humidity > 65;
                
                // Store decisions
                commands.hset("edge:control:cooling_system", "active", String.valueOf(shouldActivateCooling));
                commands.hset("edge:control:cooling_system", "last_decision", 
                    LocalDateTime.now().toString());
                
                commands.hset("edge:control:dehumidifier", "active", String.valueOf(shouldActivateDehumidifier));
                
                logger.info("  ❄️  Cooling system: {} (temp: {:.1f}°C, humidity: {:.1f}%)", 
                    shouldActivateCooling ? "ON" : "OFF", temperature, humidity);
                logger.info("  💨 Dehumidifier: {}", shouldActivateDehumidifier ? "ON" : "OFF");
                
                // Log energy usage decisions
                if (shouldActivateCooling || shouldActivateDehumidifier) {
                    commands.hincrby("edge:energy:usage", "hvac_minutes", 1);
                }
            }
            
            // Decision 2: Security system automation
            String motionStr = commands.hget("edge:iot:latest", "device_office_motion");
            String lightStr = commands.hget("edge:iot:latest", "device_office_light");
            
            if (motionStr != null && lightStr != null) {
                double motion = Double.parseDouble(motionStr);
                double light = Double.parseDouble(lightStr);
                
                // Security decisions
                boolean isAfterHours = isAfterBusinessHours();
                boolean suspiciousActivity = isAfterHours && motion > 0.5;
                boolean shouldActivateLights = motion > 0.1 && light < 50;
                
                if (suspiciousActivity) {
                    commands.lpush("edge:security:alerts", 
                        String.format("{\"type\":\"after_hours_motion\",\"motion\":%.2f,\"timestamp\":%d}", 
                        motion, System.currentTimeMillis()));
                    
                    logger.info("  🚨 Security alert: After-hours motion detected");
                }
                
                if (shouldActivateLights) {
                    commands.hset("edge:control:lighting", "auto_on", "true");
                    logger.info("  💡 Auto-lighting: ON (motion detected, low light)");
                } else {
                    commands.hset("edge:control:lighting", "auto_on", "false");
                }
            }
            
            // Decision 3: Predictive maintenance
            logger.info("🔧 Making predictive maintenance decisions...");
            
            String[] equipment = {"pump_1", "conveyor_1", "press_1"};
            
            for (String equipmentId : equipment) {
                // Simulate equipment health metrics
                double vibration = ThreadLocalRandom.current().nextDouble(0.1, 2.0);
                double temperature = ThreadLocalRandom.current().nextDouble(40, 80);
                double efficiency = ThreadLocalRandom.current().nextDouble(0.7, 1.0);
                
                // Store metrics
                commands.hset("edge:equipment:" + equipmentId, "vibration", String.valueOf(vibration));
                commands.hset("edge:equipment:" + equipmentId, "temperature", String.valueOf(temperature));
                commands.hset("edge:equipment:" + equipmentId, "efficiency", String.valueOf(efficiency));
                
                // Decision logic for maintenance
                boolean needsMaintenance = vibration > 1.5 || temperature > 70 || efficiency < 0.8;
                boolean critical = vibration > 1.8 || temperature > 75 || efficiency < 0.7;
                
                if (critical) {
                    commands.hset("edge:maintenance:" + equipmentId, "status", "critical");
                    commands.lpush("edge:maintenance:critical", equipmentId);
                    logger.info("  🔴 CRITICAL: {} requires immediate maintenance", equipmentId);
                } else if (needsMaintenance) {
                    commands.hset("edge:maintenance:" + equipmentId, "status", "scheduled");
                    commands.lpush("edge:maintenance:scheduled", equipmentId);
                    logger.info("  🟡 SCHEDULED: {} maintenance recommended", equipmentId);
                } else {
                    commands.hset("edge:maintenance:" + equipmentId, "status", "good");
                    logger.info("  🟢 OK: {} operating normally", equipmentId);
                }
            }
            
            return null;
        });
    }
    
    private void demonstrateEdgeCloudSync() {
        logger.info("☁️  Demonstrating Edge-to-Cloud Synchronization...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("📤 Preparing data for cloud synchronization...");
            
            // Collect summary data for cloud
            Map<String, Object> cloudSyncData = new HashMap<>();
            
            // Aggregate location summaries
            String[] locations = {"factory_floor", "warehouse", "office", "outdoor"};
            for (String location : locations) {
                var locationData = commands.hgetall("edge:location:" + location);
                if (!locationData.isEmpty()) {
                    cloudSyncData.put("location_" + location, locationData);
                }
            }
            
            // Energy usage summary
            var energyData = commands.hgetall("edge:energy:usage");
            if (!energyData.isEmpty()) {
                cloudSyncData.put("energy_usage", energyData);
            }
            
            // Critical alerts summary
            long criticalAlerts = commands.llen("edge:maintenance:critical");
            long securityAlerts = commands.llen("edge:security:alerts");
            
            cloudSyncData.put("alerts_summary", Map.of(
                "critical_maintenance", criticalAlerts,
                "security_incidents", securityAlerts
            ));
            
            // Store in cloud sync queue
            String syncPayload = String.format("{\"timestamp\":%d,\"edge_id\":\"edge_001\",\"data\":%s}", 
                System.currentTimeMillis(), cloudSyncData.toString());
            
            commands.lpush("edge:cloud_sync:queue", syncPayload);
            commands.expire("edge:cloud_sync:queue", 86400); // Keep for 24 hours
            
            logger.info("  📦 Queued cloud sync package with {} data points", cloudSyncData.size());
            
            // Simulate cloud acknowledgment
            logger.info("📥 Processing cloud synchronization...");
            
            // Mark successful sync
            commands.hset("edge:sync:status", "last_sync", String.valueOf(System.currentTimeMillis()));
            commands.hset("edge:sync:status", "status", "success");
            commands.hincrby("edge:sync:stats", "successful_syncs", 1);
            
            // Cleanup synchronized data
            commands.ltrim("edge:cloud_sync:queue", 0, 9); // Keep only last 10 items
            
            logger.info("  ✅ Cloud synchronization completed");
            
            // Edge backup and replication
            logger.info("💾 Creating edge backup...");
            
            String backupKey = "edge:backup:" + Instant.now().getEpochSecond();
            
            // Backup critical data
            commands.hset(backupKey, "device_count", String.valueOf(20));
            commands.hset(backupKey, "alert_count", String.valueOf(criticalAlerts + securityAlerts));
            commands.hset(backupKey, "sync_status", "complete");
            
            commands.expire(backupKey, 604800); // Keep backup for 1 week
            
            logger.info("  💾 Backup created: {}", backupKey);
            
            return null;
        });
    }
    
    private void demonstrateEdgeCoordination() {
        logger.info("🌐 Demonstrating Distributed Edge Coordination...");
        
        connectionManager.withSyncCommands(commands -> {
            logger.info("🤝 Coordinating with neighboring edge nodes...");
            
            // Simulate multiple edge nodes
            String[] edgeNodes = {"edge_001", "edge_002", "edge_003"};
            String currentNode = "edge_001";
            
            // Heartbeat mechanism
            for (String nodeId : edgeNodes) {
                long heartbeat = System.currentTimeMillis();
                commands.setex("edge:cluster:heartbeat:" + nodeId, 60, String.valueOf(heartbeat));
                
                if (nodeId.equals(currentNode)) {
                    logger.info("  💓 Heartbeat sent from {}", nodeId);
                } else {
                    logger.info("  💓 Heartbeat received from {}", nodeId);
                }
            }
            
            // Leader election simulation
            logger.info("👑 Performing leader election...");
            
            // Use Redis for distributed leader election
            String leaderKey = "edge:cluster:leader";
            String leaderLockKey = "edge:cluster:leader:lock";
            
            // Try to acquire leadership
            String lockResult = commands.set(leaderLockKey, currentNode, 
                io.lettuce.core.SetArgs.Builder.nx().ex(30));
            
            if ("OK".equals(lockResult)) {
                commands.setex(leaderKey, 30, currentNode);
                logger.info("  👑 {} elected as cluster leader", currentNode);
                
                // Leader responsibilities
                logger.info("📊 Leader coordinating cluster operations...");
                
                // Distribute workload
                commands.hset("edge:cluster:workload", "edge_001", "primary");
                commands.hset("edge:cluster:workload", "edge_002", "backup");
                commands.hset("edge:cluster:workload", "edge_003", "analytics");
                
                // Aggregate cluster metrics
                commands.hset("edge:cluster:metrics", "total_devices", "60");
                commands.hset("edge:cluster:metrics", "active_alerts", "5");
                commands.hset("edge:cluster:metrics", "cluster_health", "healthy");
                
                logger.info("  📋 Workload distributed across cluster");
            } else {
                String currentLeader = commands.get(leaderKey);
                logger.info("  👥 {} following leader: {}", currentNode, currentLeader);
            }
            
            // Shared edge cache
            logger.info("🔄 Managing shared edge cache...");
            
            // Cache frequently accessed data across edge nodes
            commands.hset("edge:shared:cache", "weather_forecast", "sunny_25c");
            commands.hset("edge:shared:cache", "traffic_status", "normal");
            commands.hset("edge:shared:cache", "energy_prices", "peak_rate");
            
            commands.expire("edge:shared:cache", 1800); // 30 minutes
            
            // Node failure detection and recovery
            logger.info("🔍 Monitoring node health...");
            
            for (String nodeId : edgeNodes) {
                String heartbeatStr = commands.get("edge:cluster:heartbeat:" + nodeId);
                
                if (heartbeatStr != null) {
                    long lastHeartbeat = Long.parseLong(heartbeatStr);
                    long timeSince = System.currentTimeMillis() - lastHeartbeat;
                    
                    if (timeSince > 120000) { // 2 minutes
                        logger.info("  ⚠️  Node {} appears to be offline", nodeId);
                        commands.hset("edge:cluster:status", nodeId, "offline");
                        
                        // Trigger failover if it's the leader
                        String currentLeader = commands.get(leaderKey);
                        if (nodeId.equals(currentLeader)) {
                            commands.del(leaderKey);
                            commands.del(leaderLockKey);
                            logger.info("  🔄 Leader failover triggered");
                        }
                    } else {
                        commands.hset("edge:cluster:status", nodeId, "online");
                        logger.info("  ✅ Node {} is healthy", nodeId);
                    }
                }
            }
            
            return null;
        });
    }
    
    // Utility methods
    private double generateIoTValue(String deviceType) {
        switch (deviceType) {
            case "temperature":
                return 20 + ThreadLocalRandom.current().nextGaussian() * 8;
            case "humidity":
                return 45 + ThreadLocalRandom.current().nextGaussian() * 15;
            case "pressure":
                return 1013 + ThreadLocalRandom.current().nextGaussian() * 20;
            case "motion":
                return ThreadLocalRandom.current().nextDouble(0, 1);
            case "light":
                return ThreadLocalRandom.current().nextDouble(0, 100);
            default:
                return ThreadLocalRandom.current().nextDouble(0, 100);
        }
    }
    
    private double calculateComfortIndex(double temperature, double humidity) {
        // Simplified comfort index calculation
        double idealTemp = 22.0;
        double idealHumidity = 50.0;
        
        double tempScore = Math.max(0, 100 - Math.abs(temperature - idealTemp) * 5);
        double humidityScore = Math.max(0, 100 - Math.abs(humidity - idealHumidity) * 2);
        
        return (tempScore + humidityScore) / 2;
    }
    
    private double getFilterThreshold(String deviceType) {
        switch (deviceType) {
            case "temperature":
                return 1.0; // Send if changed by 1°C
            case "humidity":
                return 5.0; // Send if changed by 5%
            case "pressure":
                return 2.0; // Send if changed by 2 hPa
            case "motion":
                return 0.1; // Send if changed by 0.1
            case "light":
                return 10.0; // Send if changed by 10%
            default:
                return 1.0;
        }
    }
    
    private double performLinearPrediction(List<String> values) {
        // Simple linear regression for next value prediction
        if (values.size() < 2) return 0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = Math.min(values.size(), 10); // Use last 10 values
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = Double.parseDouble(values.get(i));
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        return slope * n + intercept; // Predict next value
    }
    
    private boolean isAfterBusinessHours() {
        int hour = LocalDateTime.now().getHour();
        return hour < 8 || hour > 18; // Before 8 AM or after 6 PM
    }
}
