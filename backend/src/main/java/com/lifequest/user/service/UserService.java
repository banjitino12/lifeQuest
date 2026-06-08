package com.lifequest.user.service;

import com.lifequest.auth.model.CurrentUserPrincipal;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.user.dto.CurrentUserResponse;
import com.lifequest.user.dto.UpdateCurrentUserRequest;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UserService(CurrentUserService currentUserService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return toResponse(user);
    }

    @Transactional
    public CurrentUserResponse updateCurrentUser(UpdateCurrentUserRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        String username = normalizeOptional(request.username());
        if (StringUtils.hasText(username) && !Objects.equals(username, user.getUsername())) {
            userRepository.findByUsername(username)
                    .filter(existing -> !Objects.equals(existing.getId(), userId))
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
                    });
            user.setUsername(username);
        }

        String avatar = normalizeOptional(request.avatar());
        if (request.avatar() != null) {
            user.setAvatar(avatar);
        }

        return toResponse(userRepository.save(user));
    }

    private CurrentUserResponse toResponse(UserEntity user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getCreatedAt()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
