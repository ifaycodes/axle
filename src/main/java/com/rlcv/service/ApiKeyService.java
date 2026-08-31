package com.rlcv.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rlcv.dto.KeyRequest;
import com.rlcv.model.ApiKey;
import com.rlcv.repository.ApiKeyRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ApiKeyRepository apiKeyRepository;
    private final ObjectMapper objectMapper;

    public String generateKey(KeyRequest request) {
        String rawKey = UUID.randomUUID().toString();

        List<String> uniqueUrls = request.getUrls().stream().distinct().toList();
        apiKeyRepository.save(ApiKey.builder()
                .keyHash(DigestUtils.sha256Hex(rawKey))
                .owner(request.getOwner())
                .urls(uniqueUrls)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        return rawKey;
    }

    public void updateUrlList(List<String> newUrls, HttpServletRequest request) {
        ApiKey apiKey = (ApiKey) request.getAttribute("apiKey");
        
        newUrls.stream()
            .filter(u -> !apiKey.getUrls().contains(u))
            .forEach(apiKey.getUrls()::add);
        apiKeyRepository.save(apiKey);
    }

    public List<String> getAssociateUrls(HttpServletRequest request) {
        ApiKey apiKey = (ApiKey) request.getAttribute("apiKey");
        return apiKey.getUrls();
    }

    public Optional<ApiKey> validate(String rawKey) throws JsonMappingException, JsonProcessingException {
        String hashedKey = DigestUtils.sha256Hex(rawKey);
        String cacheKey = CacheKeys.auth(hashedKey);

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Optional.of(objectMapper.readValue(cached, ApiKey.class));
        }
        Optional<ApiKey> result = apiKeyRepository.findByKeyHashAndActiveTrue(hashedKey);
        if (result.isPresent()) {
            String json = objectMapper.writeValueAsString(result.get());
            redisTemplate.opsForValue().set(cacheKey, json);
        }
        return result;
    }

    public boolean ownsUrl(ApiKey apiKey, String url) {
        return apiKey.getUrls().stream().anyMatch(url::startsWith);
    }

    public void revokeKey(HttpServletRequest request) {
        ApiKey keyEntity = (ApiKey) request.getAttribute("apiKey");
        keyEntity.setActive(false);
        apiKeyRepository.save(keyEntity);
    }
}
