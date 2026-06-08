package com.lifequest.auth.security;

public record JwtTokenPayload(
        Long userId,
        String username,
        TokenType tokenType
) {
}
