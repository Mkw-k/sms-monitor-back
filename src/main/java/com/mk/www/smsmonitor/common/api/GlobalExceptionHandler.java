package com.mk.www.smsmonitor.common.api;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TID_KEY = "tid";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDTO<Void>> handleException(Exception e) {
        String tid = MDC.get(TID_KEY);
        log.error("[TID: {}] Global Exception: {}", tid, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultDTO.error("SERVER_ERROR", e.getMessage(), tid));
    }
}
