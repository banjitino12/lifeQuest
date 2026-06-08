package com.lifequest.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USERNAME_CLAIM = "username";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, TokenType.ACCESS, Instant.now().plusSeconds(properties.getAccessTokenTtlMinutes() * 60));
    }

    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, TokenType.REFRESH, Instant.now().plusSeconds(properties.getRefreshTokenTtlDays() * 24 * 60 * 60));
    }

    public JwtTokenPayload parseAccessToken(String token) {
        return parseToken(token, TokenType.ACCESS);
    }

    public JwtTokenPayload parseRefreshToken(String token) {
        return parseToken(token, TokenType.REFRESH);
    }

    private String generateToken(Long userId, String username, TokenType tokenType, Instant expiresAt) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .claim(USERNAME_CLAIM, username)
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    private JwtTokenPayload parseToken(String token, TokenType expectedTokenType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            TokenType actualTokenType = TokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class));
            if (actualTokenType != expectedTokenType) {
                throw new JwtException("Unexpected token type");
            }
            return new JwtTokenPayload(
                    Long.valueOf(claims.getSubject()),
                    claims.get(USERNAME_CLAIM, String.class),
                    actualTokenType
            );
        } catch (RuntimeException exception) {
            throw new JwtException("Invalid JWT token", exception);
        }
    }
}
