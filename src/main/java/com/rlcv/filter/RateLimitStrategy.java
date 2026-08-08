package com.rlcv.filter;

public interface RateLimitStrategy {
    boolean isAllowed(String key);
}
