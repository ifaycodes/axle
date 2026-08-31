package com.rlcv.filter;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("log")
@RequiredArgsConstructor
public class SlidingWindowLog implements RateLimitStrategy {

    private final RedisTemplate<String, String> redisTemplate;

    
    @Value("${rate.limit.window}")
    private long windowSizeInSeconds;

    @Value("${rate.limit.requests}")
    private long maxRequests;

    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSizeInSeconds * 1000);

        String redisKey = "ratelimit:log" + key;

        // remove older timestamps and count how many timestamps remain
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(redisKey);

        // if under limit, add current timestamp
        if (count == null || count < maxRequests) {
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSizeInSeconds));
            return true;
        }

        // over limit, reject it
        return false;
    }
}