package com.rlcv.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlcv.dto.KeyRequest;
import com.rlcv.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;


    @PostMapping("/generate")
    @Operation(summary = "Generate an API key to authorization")
    public ResponseEntity<Map<String, String>> generate(@RequestBody @Valid KeyRequest request) {
        String rawKey = apiKeyService.generateKey(request);

        return ResponseEntity.ok(Map.of(
            "apiKey", rawKey,
            "owner", request.getOwner(),
            "urls", request.getUrls().toString()
        ));
    }

    @PostMapping("/url-update")
    @Operation(summary = "Update URL associated to a key")
    public ResponseEntity<String> addNewUrls(HttpServletRequest httpRequest, @RequestBody List<String> urls) {
        apiKeyService.updateUrlList(urls, httpRequest);
        return ResponseEntity.ok("URL list updated");
    }

    @GetMapping("/linked-url")
    @Operation(summary = "Return a list of the keys associated to this user")
    public ResponseEntity<List<String>> listLinkedUrl(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(apiKeyService.getAssociateUrls(httpRequest));
    }

    @DeleteMapping("/revoke")
    @Operation(summary = "Revoke an ApiKey. Change Active state to False")
    public ResponseEntity<String> revokeKey(HttpServletRequest httpRequest) {
        apiKeyService.revokeKey(httpRequest);
        return ResponseEntity.ok("Key Deactivated");
    }
}
