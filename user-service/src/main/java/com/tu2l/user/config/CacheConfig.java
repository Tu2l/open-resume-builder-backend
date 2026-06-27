package com.tu2l.user.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Set;

/**
 * Cache configuration. In single-node (dev) deployments uses an in-process Caffeine cache.
 * In multi-node (prod, redis-enabled=true) deployments uses Redis so that cache evictions on
 * any node are immediately visible to all other nodes — preventing stale role/status data.
 *
 * <p>Note: {@code UserEntity} lazy associations (profile, accountStatus, credentials) are not
 * initialized when the entity is cached; callers must never access those associations on a
 * cache hit. See the {@code @Cacheable} comment in {@code UserServiceImpl.getUserByUsername}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERS_CACHE = "users";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.redis-enabled", havingValue = "false", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(USERS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(CACHE_TTL)
                .maximumSize(1000));
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.redis-enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CACHE_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JdkSerializationRedisSerializer()));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .initialCacheNames(Set.of(USERS_CACHE))
                .build();
    }
}
