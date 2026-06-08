package com.lifequest.common.exception;

public record ValidationErrorDetail(
        String field,
        String reason
) {
}
