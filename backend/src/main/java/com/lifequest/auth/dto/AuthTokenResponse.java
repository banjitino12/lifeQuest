package com.lifequest.auth.dto;

public record AuthTokenResponse(
        Long userId,
        String username,
        String accessToken,
        String refreshToken,
        boolean profileCompleted
) {
}
