package com.rlcv.filter;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component("counter")
public class SlidingWindowCounter implements RateLimitStrategy{

    private RedisTemplate<String, String> redisTemplate;
    private long windowSizeInSeconds;
    private long maxRequests;

    @Override
    public boolean isAllowed (String key) {
        long now = System.currentTimeMillis() / 1000;
        long currentwindow = now / windowSizeInSeconds;
        long previousWindow = currentwindow - 1;

        String currentKey = "ratelimit:counter:" + key + ":" + currentwindow;
        String previousKey = "ratelimit:counter:" + key + ":" + previousWindow;

        List<String> counts = redisTemplate.opsForValue().multiGet(List.of(currentKey, previousKey));
        long currentCount = counts.get(0) != null ? Long.parseLong(counts.get(0)) : 0;
        long previousCount = counts.get(1) != null ? Long.parseLong(counts.get(1)) : 0;

        double timePassedInWindow = (now % windowSizeInSeconds) / (double) windowSizeInSeconds;

        long weightedCount =  Math.round((previousCount * (1 - timePassedInWindow)) + currentCount);
        
        if (weightedCount < maxRequests) {
            redisTemplate.opsForValue().increment(currentKey);
            redisTemplate.expire(currentKey, Duration.ofSeconds(windowSizeInSeconds * 2));
            
            return true;
        }

        return false;

    }
}
