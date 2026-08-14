package com.mk.www.smsmonitor.common.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class ApiResponse<T> {
    private final String tid;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.tid = UUID.randomUUID().toString();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>("SUCCESS", "리소스가 성공적으로 생성되었습니다.", data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
