package com.mk.www.smsmonitor.transaction.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "메모 수정 요청 DTO")
public class MemoRequest {
    @Schema(description = "수정할 메모 내용", example = "친구와 저녁 식사")
    private String memo;
}
