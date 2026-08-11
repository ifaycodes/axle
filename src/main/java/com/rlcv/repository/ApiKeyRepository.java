package com.rlcv.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rlcv.model.ApiKey;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    boolean existsByKeyHashAndActiveTrue(String hashed);
}
