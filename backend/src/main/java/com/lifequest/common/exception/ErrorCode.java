package com.lifequest.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    OK("OK", "success", HttpStatus.OK),
    CREATED("CREATED", "created", HttpStatus.CREATED),
    VALIDATION_ERROR("VALIDATION_ERROR", "参数校验失败", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "未登录或 Token 无效", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "无权限", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", "资源冲突", HttpStatus.CONFLICT),
    RATE_LIMITED("RATE_LIMITED", "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    LLM_UNAVAILABLE("LLM_UNAVAILABLE", "LLM 不可用，但基础结果可降级返回", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
