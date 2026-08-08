package com.rlcv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.rlcv.filter.RateLimitStrategy;
import com.rlcv.filter.SlidingWindowCounter;
import com.rlcv.filter.SlidingWindowLog;

@Configuration
public class RateLimitConfig {

    @Value("${rate.limit.strategy}")
    private String strategy;

    @Bean
    @Primary
    public RateLimitStrategy rateLimitStrategy(
            SlidingWindowLog log,
            SlidingWindowCounter counter) {
        return strategy.equals("log") ? log : counter;
    }
}
