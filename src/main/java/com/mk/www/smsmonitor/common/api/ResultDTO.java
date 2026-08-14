package com.mk.www.smsmonitor.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO<T> {
    private String tid;       // 트랜잭션 ID (UUID)
    private String code;      // 응답 코드 (예: "SUCCESS", "ERROR_001")
    private String message;   // 응답 메시지
    private T data;           // 실제 응답 데이터

    public static <T> ResultDTO<T> success(T data, String tid) {
        return ResultDTO.<T>builder()
                .tid(tid)
                .code("SUCCESS")
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    public static <T> ResultDTO<T> error(String code, String message, String tid) {
        return ResultDTO.<T>builder()
                .tid(tid)
                .code(code)
                .message(message)
                .build();
    }
}
