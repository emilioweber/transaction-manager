package com.wex.purchasetransaction.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        Map<String, String> details,
        LocalDateTime timestamp
) {

    private static ApiError from(
            HttpStatus status,
            String message,
            Map<String, String> details
    ) {
        return new ApiError(
                status.value(),
                status.name(),
                message,
                details,
                LocalDateTime.now()
        );
    }

    public static ApiError validationError(Map<String, String> details) {
        return from(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    public static ApiError unauthorized(String message) {
        return from(HttpStatus.UNAUTHORIZED, message, null);
    }

    public static ApiError conflict(String message) {
        return from(HttpStatus.CONFLICT, message, null);
    }

    public static ApiError internalError() {
        return from(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
        );
    }

    public static ApiError of(HttpStatus status, String message) {
        return from(status, message, null);
    }
}
