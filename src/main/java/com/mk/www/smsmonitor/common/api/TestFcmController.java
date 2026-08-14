package com.mk.www.smsmonitor.common.api;

import com.mk.www.smsmonitor.common.application.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestFcmController {
    private final FcmService fcmService;

    @GetMapping("/api/test/fcm")
    public String testFcm(@RequestParam String token) {
        fcmService.sendMessage(token, "테스트 알림", "백엔드에서 보낸 메시지입니다!");
        return "Push sent to: " + token;
    }
}
