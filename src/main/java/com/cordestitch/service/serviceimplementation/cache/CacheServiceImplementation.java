package com.cordestitch.service.serviceimplementation.cache;

import com.cordestitch.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CacheServiceImplementation {

    //This service is only for the testing purpose in dev environment

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    public void flushRedisCache() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (!isNull(connectionFactory)) {
            connectionFactory.getConnection().serverCommands().flushDb();
        }
    }

    public void flushCaffeineCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (!isNull(cacheManager.getCache(cacheName))) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            }
        });
    }

    public SuccessResponse flushAllCaches() {
        flushRedisCache();
        flushCaffeineCache();
        return new SuccessResponse("Cache flushed successfully", HttpStatus.OK.value());
    }
}