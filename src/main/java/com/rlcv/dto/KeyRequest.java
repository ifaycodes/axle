package com.rlcv.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class KeyRequest {
    @NotBlank(message = "owner is required")
    private String owner;

    @NotEmpty(message = "at least one url is required")
    private List<String> urls;
}
