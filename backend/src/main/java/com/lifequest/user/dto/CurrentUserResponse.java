package com.lifequest.user.dto;

import java.time.LocalDateTime;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        String phone,
        String avatar,
        LocalDateTime createdAt
) {
}
