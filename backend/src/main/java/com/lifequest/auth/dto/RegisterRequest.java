package com.lifequest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        String email,
        String phone,
        @NotBlank @Size(min = 8, max = 64) String password
) {
}
