package com.lifequest.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifequest.auth.dto.AuthTokenResponse;
import com.lifequest.auth.dto.LoginRequest;
import com.lifequest.auth.dto.RefreshTokenRequest;
import com.lifequest.auth.dto.RegisterRequest;
import com.lifequest.auth.security.JwtProperties;
import com.lifequest.auth.security.JwtTokenService;
import com.lifequest.common.enums.AccountStatus;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTests {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("lifequest-test-secret-change-me-please-32-bytes");
        jwtTokenService = new JwtTokenService(properties);
        authService = new AuthService(userRepository, userProfileRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void registerEncryptsPasswordAndReturnsTokens() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        AuthTokenResponse response = authService.register(new RegisterRequest(
                " tiantian ",
                "tiantian@example.com",
                "",
                "password123"
        ));

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.username()).isEqualTo("tiantian");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("tiantian")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "tiantian",
                "tiantian@example.com",
                "",
                "password123"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void loginRejectsWrongPassword() {
        UserEntity user = activeUser(7L, "tiantian", passwordEncoder.encode("password123"));
        when(userRepository.findByUsername("tiantian")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("tiantian", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void refreshAcceptsRefreshTokenAndRejectsAccessToken() {
        UserEntity user = activeUser(7L, "tiantian", passwordEncoder.encode("password123"));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        String refreshToken = jwtTokenService.generateRefreshToken(7L, "tiantian");
        AuthTokenResponse response = authService.refresh(new RefreshTokenRequest(refreshToken));

        assertThat(response.userId()).isEqualTo(7L);

        String accessToken = jwtTokenService.generateAccessToken(7L, "tiantian");
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(accessToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    private UserEntity activeUser(Long id, String username, String passwordHash) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }
}
