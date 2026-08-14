package com.rlcv.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rlcv.model.ApiKey;
import com.rlcv.service.ApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter{

    private ApiKeyService apiKeyService;

    protected boolean shouldNotfilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/keys/generate") || path.equals("analytics/top") || path.equals("analytics/top/hourly") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String rawKey = request.getHeader("X-API-KEY");

        if (rawKey == null) {
            reject(response, "Missing API key");
            return;
        }

        Optional<ApiKey> apiKey = apiKeyService.validate(rawKey);

        if (apiKey.isEmpty()) {
            reject(response, "Invalid API key");
            return;
        }

        request.setAttribute("apiKey", apiKey.get());
        filterChain.doFilter(request, response);

    }

    private void reject (HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
            
    }
}
