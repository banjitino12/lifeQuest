package com.lifequest.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(
        @Size(min = 1, max = 64) String username,
        @Size(max = 512) String avatar
) {
}
