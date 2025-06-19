# Redis Advanced Use Cases with Java & Lettuce

A comprehensive showcase of advanced Redis patterns and use cases using Java 17, Gradle, and the Lettuce Redis client library.

## 🚀 Features

This project demonstrates the following Redis patterns and advanced use cases:

### 1. **Caching Strategies** ✅
- **Cache-aside pattern** - Lazy loading with cache invalidation
- **Write-through caching** - Synchronous cache and database updates
- **Write-behind caching** - Asynchronous background persistence
- **Cache warming** - Preloading cache with pipelining
- **TTL management** - Automatic expiration and extension
- **Cache metrics** - Hit/miss ratios and performance tracking

### 2. **Pub/Sub Messaging** ✅
- **Real-time subscriber/publisher** - Live message broadcasting
- **Multiple channel subscriptions** - notifications, events, alerts
- **Message routing** - Channel-based message distribution
- **Event-driven architecture** - Decoupled system communication
- **Subscriber lifecycle management** - Connection handling and cleanup

### 3. **Redis Streams** ✅
- **Event streaming** - Producer/consumer message queues
- **Consumer groups** - Distributed message processing
- **Stream acknowledgments** - Reliable message delivery
- **Multiple stream management** - events, notifications, analytics
- **Message persistence** - Durable event log storage

### 4. **Distributed Systems** ✅
- **Distributed locks** - Resource synchronization with expiration
- **Rate limiting algorithms** - Fixed window, sliding window, token bucket
- **Atomic operations** - Transaction-based multi-command execution
- **Lua scripting** - Server-side atomic computations
- **Connection pooling** - Efficient resource management

### 5. **Advanced Data Structures** ✅
- **HyperLogLog** - Cardinality estimation for unique visitors
- **Geospatial operations** - Location-based queries and distance calculations
- **Sets and Sorted Sets** - Collection operations and ranking
- **Hash operations** - Token bucket implementation
- **Pipeline operations** - Batch command execution

### 6. **Monitoring & Health** ✅
- **Micrometer metrics** - Comprehensive performance monitoring
- **Health checks** - Periodic Redis connectivity validation
- **Connection pool monitoring** - Resource utilization tracking
- **Error tracking** - Connection and operation error metrics
- **Structured logging** - Detailed operational insights

## 🛠️ Technology Stack

- **Java 17**: Modern Java features and performance
- **Gradle 8.7**: Build automation and dependency management
- **Lettuce 6.4**: Reactive Redis client with async/sync APIs
- **Jackson**: JSON serialization/deserialization
- **Micrometer**: Metrics and monitoring
- **TestContainers**: Integration testing with Redis
- **JUnit 5**: Modern testing framework

## 📁 Project Structure

```
src/
├── main/java/com/example/redis/
│   ├── cache/          # Caching strategies and patterns
│   ├── pubsub/         # Publish/Subscribe messaging
│   ├── streams/        # Redis Streams implementation
│   ├── lock/           # Distributed locking mechanisms
│   ├── ratelimit/      # Rate limiting implementations
│   ├── data/           # Advanced data structure operations
│   ├── monitoring/     # Metrics and health checks
│   ├── config/         # Configuration management
│   └── utils/          # Utility classes and helpers
└── test/               # Comprehensive test suite
```

## 🚦 Quick Start

### Prerequisites
- Java 17+
- Redis 7.0+ (or use Docker)
- Gradle 8.7+ (or use included wrapper)

### 1. Start Redis
Using Docker:
```bash
docker run -d --name redis-advanced -p 6379:6379 redis:7.2-alpine redis-server --appendonly yes
```

Or install Redis locally and start:
```bash
redis-server
```

### 2. Build the Project
```bash
./gradlew build
```

### 3. Run Demonstrations

#### All Features Demo
```bash
./gradlew run
```

#### Individual Demos
```bash
# Caching patterns
./gradlew runCacheDemo

# Pub/Sub messaging
./gradlew runPubSubDemo

# Redis Streams
./gradlew runStreamsDemo

# Distributed locks
./gradlew runDistributedLockDemo

# Rate limiting
./gradlew runRateLimitDemo
```

### 4. Run Tests
```bash
# Run all tests (includes TestContainers)
./gradlew test

# Run tests with Redis container
./gradlew test --info
```

## 📖 Detailed Examples

### Cache-Aside Pattern
```java
@Component
public class UserService {
    
    @Autowired
    private RedisTemplate<String, User> redisTemplate;
    
    public User getUser(String userId) {
        // Try cache first
        User user = redisTemplate.opsForValue().get("user:" + userId);
        
        if (user == null) {
            // Load from database
            user = userRepository.findById(userId);
            
            // Store in cache
            redisTemplate.opsForValue().set("user:" + userId, user, Duration.ofMinutes(30));
        }
        
        return user;
    }
}
```

### Distributed Lock
```java
public class DistributedLock {
    
    public boolean acquireLock(String resource, String clientId, Duration timeout) {
        String lockKey = "lock:" + resource;
        String lockValue = clientId + ":" + System.currentTimeMillis();
        
        // Use SET with NX and EX options
        return redisTemplate.execute(connection -> {
            return connection.set(lockKey.getBytes(), lockValue.getBytes(), 
                Expiration.from(timeout), RedisStringCommands.SetOption.ifAbsent());
        });
    }
}
```

### Rate Limiting
```java
public class RateLimiter {
    
    public boolean isAllowed(String key, int limit, Duration window) {
        String script = 
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return current <= tonumber(ARGV[2])";
            
        return redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class),
            Collections.singletonList(key), 
            window.getSeconds(), 
            limit);
    }
}
```

## 🧪 Testing

The project includes comprehensive tests:

- **Unit Tests**: Individual component testing
- **Integration Tests**: Redis integration with TestContainers
- **Performance Tests**: Load testing and benchmarks
- **Chaos Tests**: Failure scenarios and recovery

```bash
# Run specific test categories
./gradlew test --tests "*CacheTest"
./gradlew test --tests "*PubSubTest"
./gradlew test --tests "*StreamsTest"
```

## 📊 Monitoring & Metrics

The project includes built-in monitoring:

- **Micrometer Integration**: Metrics collection
- **Health Checks**: Redis connectivity monitoring
- **Performance Metrics**: Operation latencies and throughput
- **Business Metrics**: Cache hit rates, message processing rates

Access metrics at: `http://localhost:8080/actuator/metrics`

## 🔧 Configuration

Configuration is managed through `application.conf`:

```hocon
redis {
    host = "localhost"
    port = 6379
    password = null
    database = 0
    
    pool {
        max-total = 100
        max-idle = 50
        min-idle = 10
    }
    
    cache {
        default-ttl = "30m"
        max-entries = 10000
    }
    
    streams {
        consumer-group = "app-group"
        block-time = "1s"
    }
}
```

## 🎯 Best Practices Demonstrated

1. **Connection Management**: Proper connection pooling and lifecycle
2. **Error Handling**: Resilient operations with circuit breakers
3. **Serialization**: Efficient JSON serialization strategies
4. **Memory Management**: TTL policies and eviction strategies
5. **Security**: Authentication and SSL/TLS configuration
6. **Performance**: Pipelining and batch operations
7. **Monitoring**: Comprehensive observability

## 🚀 Advanced Features

### Lua Scripting
```java
public class LuaScriptManager {
    
    private final RedisScript<Long> atomicIncrementScript = 
        new DefaultRedisScript<>(
            "return redis.call('HINCRBY', KEYS[1], KEYS[2], ARGV[1])",
            Long.class);
    
    public Long atomicIncrement(String hash, String field, Long delta) {
        return redisTemplate.execute(atomicIncrementScript, 
            Arrays.asList(hash, field), delta);
    }
}
```

### Reactive Operations
```java
@Service
public class ReactiveRedisService {
    
    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    public Mono<String> setAsync(String key, Object value) {
        return reactiveRedisTemplate.opsForValue()
            .set(key, value)
            .then(Mono.just("OK"));
    }
    
    public Flux<String> getPattern(String pattern) {
        return reactiveRedisTemplate.scan(ScanOptions.scanOptions()
            .match(pattern)
            .build());
    }
}
```

## ✅ Implementation Status

All major Redis advanced use cases have been successfully implemented and tested:

### **Core Features Completed:**

🗄️ **Cache Patterns**
- ✅ Cache-aside with User repository simulation
- ✅ Write-through pattern with immediate persistence
- ✅ Write-behind pattern with asynchronous queuing
- ✅ Cache warming using pipelining for bulk operations
- ✅ TTL management with automatic expiration and extension
- ✅ Cache metrics and hit/miss ratio tracking

📡 **Pub/Sub Messaging** 
- ✅ Real-time subscriber/publisher with async message handling
- ✅ Multi-channel subscriptions (notifications, events, alerts)
- ✅ Message lifecycle management with proper connection handling
- ✅ Event-driven architecture demonstration

🌊 **Redis Streams**
- ✅ Producer/consumer pattern with multiple streams
- ✅ Consumer groups with message acknowledgments
- ✅ Stream data persistence and retrieval
- ✅ Multi-stream processing (events, notifications, analytics)

🔒 **Distributed Systems**
- ✅ Distributed locks with client identification and expiration
- ✅ Rate limiting using Lua scripts for atomicity
- ✅ Connection pooling with Apache Commons Pool
- ✅ Atomic operations using Redis transactions

📊 **Advanced Data Operations**
- ✅ HyperLogLog for unique visitor cardinality estimation
- ✅ Geospatial operations with distance calculations
- ✅ Lua scripting for server-side atomic computations
- ✅ Pipeline operations for batch command execution

🔍 **Monitoring & Health**
- ✅ Micrometer metrics integration
- ✅ Real-time health checks with configurable intervals
- ✅ Connection pool monitoring and statistics
- ✅ Error tracking and performance metrics
- ✅ Structured logging with logback configuration

🧪 **Testing Infrastructure**
- ✅ Unit tests for configuration validation
- ✅ Integration tests with TestContainers support
- ✅ Comprehensive test coverage for core functionality

### **Demo Output Sample:**
```
🚀 === Redis Advanced Use Cases Demo ===
✅ Redis connectivity verified
✅ Monitoring started

🗄️ --- Cache Patterns Demonstration ---
✅ Cache-aside, write-through, write-behind patterns working
✅ Cache warming completed for 3 users
📊 Cache Statistics: Hits: 2, Misses: 1, Hit Ratio: 66.67%

📡 --- Pub/Sub Messaging Demonstration ---
📡 Subscribed to channels: notifications:user, events:order, alerts:system
📥 Real-time message delivery working perfectly

🌊 --- Redis Streams Demonstration ---
📝 Producer created events in multiple streams
👥 Consumer groups processing messages with acknowledgments
📥 Processed 3 events, 2 notifications, 2 analytics messages

🔒 --- Distributed Lock Demonstration ---
✅ Lock acquired and released successfully with client identification

⏱️ --- Rate Limiting Demonstration ---
✅ Rate limiting working with configurable limits

📊 Redis Metrics: Connection pool, health checks, and performance monitoring active
```

## 📚 Learning Resources

- [Redis Documentation](https://redis.io/documentation)
- [Lettuce Documentation](https://lettuce.io/core/release/reference/)
- [Redis Patterns](https://redis.io/topics/patterns)
- [Java Redis Best Practices](https://developer.redis.com/develop/java/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Redis Advanced Use Cases** - Showcasing professional Redis patterns with Java and Lettuce 🚀
