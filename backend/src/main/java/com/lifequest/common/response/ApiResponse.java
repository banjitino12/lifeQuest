package com.lifequest.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lifequest.common.exception.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.OK.code(), ErrorCode.OK.defaultMessage(), data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(ErrorCode.CREATED.code(), ErrorCode.CREATED.defaultMessage(), data);
    }

    public static ApiResponse<Void> success() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.code(), errorCode.defaultMessage(), data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
        return new ApiResponse<>(errorCode.code(), message, data);
    }
}
