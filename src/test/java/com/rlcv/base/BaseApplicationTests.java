package com.rlcv.base;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.rlcv.dto.KeyRequest;
import com.rlcv.model.ApiKey;
import com.rlcv.repository.ApiKeyRepository;
import com.rlcv.service.ApiKeyService;

@SpringBootTest
class BaseApplicationTests {

	@Autowired
	private ApiKeyRepository apiKeyRepository;

	@Autowired
	private ApiKeyService apiKeyService;

	@Test
	void checkApiKeyInactiveSession () {
		KeyRequest request = new KeyRequest();
		request.setOwner("owner");
		request.setUrls(List.of("owner.com"));
		
		String rawKey = apiKeyService.generateKey(request);
		String hashKey = DigestUtils.sha256Hex(rawKey);

		Optional<ApiKey> activeApi = apiKeyRepository.findByKeyHashAndActiveTrue(hashKey);
		assertTrue(activeApi.isPresent());

		ApiKey entity = activeApi.get();
		entity.setActive(false);
		apiKeyRepository.save(entity);

		Optional<ApiKey> notActiveApi = apiKeyRepository.findByKeyHashAndActiveTrue(hashKey);
		assertFalse(notActiveApi.isPresent());
	}


}
