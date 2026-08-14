package com.rlcv.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import com.rlcv.dto.KeyRequest;
import com.rlcv.model.ApiKey;
import com.rlcv.repository.ApiKeyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private ApiKeyRepository apiKeyRepository;

    public String generateKey(KeyRequest request) {
        String rawKey = UUID.randomUUID().toString();

        apiKeyRepository.save(ApiKey.builder()
                .keyHash(DigestUtils.sha256Hex(rawKey))
                .owner(request.getOwner())
                .urls(request.getUrls())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        return rawKey;
    }

    public Optional<ApiKey> validate(String rawKey) {
        return apiKeyRepository.findByKeyHashAndActiveTrue(DigestUtils.sha256Hex(rawKey));
    }

    public boolean ownsUrl(ApiKey apiKey, String url) {
        return apiKey.getUrls().stream().anyMatch(url::startsWith);
    }
}
