package com.lifequest.common.exception;

import com.lifequest.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, exception.getMessage(), exception.data()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        List<ValidationErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationErrorDetail)
                .toList();
        return validationError(details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception
    ) {
        List<ValidationErrorDetail> details = exception.getAllValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ValidationErrorDetail(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage()
                        )))
                .toList();
        return validationError(details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        List<ValidationErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(this::toValidationErrorDetail)
                .toList();
        return validationError(details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ValidationErrorDetail>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        ValidationErrorDetail detail = new ValidationErrorDetail(
                exception.getParameterName(),
                "缺少必填参数"
        );
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, errorCode.defaultMessage(), detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ValidationErrorDetail>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {
        ValidationErrorDetail detail = new ValidationErrorDetail("body", "请求体格式错误");
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, errorCode.defaultMessage(), detail));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return error(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return error(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException exception) {
        ErrorCode errorCode = switch (exception.getStatusCode().value()) {
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 409 -> ErrorCode.CONFLICT;
            case 429 -> ErrorCode.RATE_LIMITED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
        String message = exception.getReason() == null ? errorCode.defaultMessage() : exception.getReason();
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, message, null));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleErrorResponseException(ErrorResponseException exception) {
        ErrorCode errorCode = switch (exception.getStatusCode().value()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.VALIDATION_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
        return error(errorCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return error(ErrorCode.INTERNAL_ERROR);
    }

    private ValidationErrorDetail toValidationErrorDetail(FieldError error) {
        return new ValidationErrorDetail(error.getField(), error.getDefaultMessage());
    }

    private ValidationErrorDetail toValidationErrorDetail(ConstraintViolation<?> violation) {
        return new ValidationErrorDetail(
                violation.getPropertyPath().toString(),
                violation.getMessage()
        );
    }

    private ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> validationError(
            List<ValidationErrorDetail> details
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, errorCode.defaultMessage(), details));
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.error(errorCode, null));
    }
}
