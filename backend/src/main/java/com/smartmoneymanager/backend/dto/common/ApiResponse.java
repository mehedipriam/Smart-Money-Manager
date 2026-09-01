package com.smartmoneymanager.backend.dto.common;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Standard envelope for every REST response, success or failure, so the
 * frontend can rely on one shape: {@code { success, message, data, errors, timestamp } }.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    /** Field -> validation message, populated only for 400 validation failures. */
    private final Map<String, String> errors;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder().success(true).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder().success(false).message(message).errors(errors).build();
    }
}
