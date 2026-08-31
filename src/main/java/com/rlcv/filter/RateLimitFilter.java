package com.rlcv.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rlcv.model.ApiKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitStrategy rateLimitStrategy;

    @Value("${rate.limit.key}")
    private String keyStrategy;

    @Override
    protected void doFilterInternal (
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String key = resolveKey(request);

        if (!rateLimitStrategy.isAllowed(key)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests, slow down.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey (HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        ApiKey apiKey = (ApiKey) request.getAttribute("apiKey");

        return switch (keyStrategy) {
            case "ip" -> ipAddress;
            case "endpoint" -> endpoint;
            case "apiKey" -> apiKey != null ? apiKey.getKeyHash() : ipAddress;
            case "ip+endpoint" -> ipAddress + ":" + endpoint;
            case "apikey+endpoint" -> (apiKey != null ? apiKey : ipAddress) + ":" + endpoint;
            default -> ipAddress;
        };
    }

}
