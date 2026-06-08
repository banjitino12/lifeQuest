package com.lifequest.auth.service;

import com.lifequest.auth.dto.AuthTokenResponse;
import com.lifequest.auth.dto.LoginRequest;
import com.lifequest.auth.dto.RefreshTokenRequest;
import com.lifequest.auth.dto.RegisterRequest;
import com.lifequest.auth.security.JwtTokenPayload;
import com.lifequest.auth.security.JwtTokenService;
import com.lifequest.common.enums.AccountStatus;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        String username = normalizeRequired(request.username());
        String email = normalizeOptional(request.email());
        String phone = normalizeOptional(request.phone());
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "邮箱和手机号至少填写一个");
        }
        assertUnique(username, email, phone);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);
        return toAuthTokenResponse(savedUser, false);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        UserEntity user = findByAccount(request.account())
                .filter(candidate -> candidate.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return toAuthTokenResponse(user, userProfileRepository.existsByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        try {
            JwtTokenPayload payload = jwtTokenService.parseRefreshToken(request.refreshToken());
            UserEntity user = userRepository.findById(payload.userId())
                    .filter(candidate -> candidate.getStatus() == AccountStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
            return toAuthTokenResponse(user, userProfileRepository.existsByUserId(user.getId()));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void assertUnique(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
        }
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
        }
    }

    private Optional<UserEntity> findByAccount(String account) {
        String normalizedAccount = normalizeRequired(account);
        return userRepository.findByUsername(normalizedAccount)
                .or(() -> userRepository.findByEmail(normalizedAccount))
                .or(() -> userRepository.findByPhone(normalizedAccount));
    }

    private AuthTokenResponse toAuthTokenResponse(UserEntity user, boolean profileCompleted) {
        return new AuthTokenResponse(
                user.getId(),
                user.getUsername(),
                jwtTokenService.generateAccessToken(user.getId(), user.getUsername()),
                jwtTokenService.generateRefreshToken(user.getId(), user.getUsername()),
                profileCompleted
        );
    }

    private String normalizeRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOptional(String value) {
        String normalized = normalizeRequired(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
