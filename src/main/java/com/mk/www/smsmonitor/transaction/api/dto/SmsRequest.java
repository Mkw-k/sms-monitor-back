package com.mk.www.smsmonitor.transaction.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SMS 수신 요청 DTO")
public class SmsRequest {
    @Schema(description = "발신자 번호", example = "010-1234-5678")
    private String sender;
    @Schema(description = "SMS 메시지 내용", example = "KB국민카드 승인 10,000원...")
    private String message;
}
