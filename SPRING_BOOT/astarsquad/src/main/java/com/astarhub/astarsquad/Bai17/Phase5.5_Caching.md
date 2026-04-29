# ⚡ Phase 5.5: Caching — Spring Cache + Redis

---

## 📑 Mục Lục

- [1. Caching Là Gì?](#1-caching-là-gì)
- [2. Spring Cache Annotations](#2-spring-cache-annotations)
- [3. Redis Setup](#3-redis-setup)
- [4. Khi Nào Cache](#4-khi-nào-cache)
- [✅ Checklist](#-checklist)

---

## 1. Caching Là Gì?

Lưu kết quả query/tính toán vào bộ nhớ nhanh. Request giống → trả cache thay vì query DB lại.

```
Cache MISS: Request → DB query (100ms) → Lưu cache → Response
Cache HIT:  Request → Cache (1ms) → Response  ← 100x nhanh hơn
```

---

## 2. Spring Cache Annotations

```java
@Configuration
@EnableCaching  // Kích hoạt caching
public class CacheConfig {}
```

```java
@Service
public class ProductService {

    // @Cacheable: Cache kết quả. Lần sau → trả cache, KHÔNG chạy method
    @Cacheable(value = "products", key = "#id")
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
            .map(ProductResponse::from).orElseThrow();
    }

    // @CacheEvict: Xóa cache khi data thay đổi
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public ProductResponse update(Long id, UpdateRequest req) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setName(req.name());
        return ProductResponse.from(productRepository.save(p));
    }

    // @CachePut: Luôn chạy method, update cache với kết quả mới
    @CachePut(value = "products", key = "#id")
    public ProductResponse refresh(Long id) {
        return ProductResponse.from(productRepository.findById(id).orElseThrow());
    }

    // Xóa toàn bộ cache
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) { productRepository.deleteById(id); }

    // Conditional caching
    @Cacheable(value = "products", key = "#id",
               unless = "#result == null", condition = "#id > 0")
    public ProductResponse findSafe(Long id) { ... }
}
```

---

## 3. Redis Setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

```java
@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "products", config.entryTtl(Duration.ofHours(1)),
            "users", config.entryTtl(Duration.ofMinutes(15))
        );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

---

## 4. Khi Nào Cache

| ✅ Cache | ❌ Không cache |
|---------|--------------|
| Data ít thay đổi | Data real-time |
| Query chậm | Data nhạy cảm |
| API gọi nhiều | Data thay đổi liên tục |

---

## ✅ Checklist

- [ ] @EnableCaching + @Cacheable/@CacheEvict/@CachePut
- [ ] Redis setup + TTL config
- [ ] Hiểu Cache HIT vs MISS
- [ ] @CacheEvict khi data thay đổi

---

> **Tiếp theo**: Đọc `Phase5.6_Async_Scheduling.md` →
