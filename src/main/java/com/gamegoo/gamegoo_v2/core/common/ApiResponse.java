package com.gamegoo.gamegoo_v2.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {

    private final int status;

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @Builder
    private ApiResponse(HttpStatus status, String message, String code, T data) {
        this.status = status.value();
        this.message = message;
        this.code = code;
        this.data = data;
    }

    // 실패 응답
    public static <T> ApiResponse<T> of(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message) {
        return ApiResponse.<T>builder()
                .status(httpStatus)
                .message(message)
                .code("VALUE_ERROR")
                .data(null)
                .build();
    }

    // 성공응답
    private static <T> ApiResponse<T> of(HttpStatus status, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(status.name())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, data);
    }

}
