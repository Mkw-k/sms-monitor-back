package com.mk.www.smsmonitor.common.api;

import com.mk.www.smsmonitor.common.api.ApiResponse;
import com.mk.www.smsmonitor.common.application.FcmService;
import com.mk.www.smsmonitor.common.util.CurrentUser;
import com.mk.www.smsmonitor.user.domain.Device;
import com.mk.www.smsmonitor.user.domain.User;
import com.mk.www.smsmonitor.user.infrastructure.DeviceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Device", description = "기기 및 푸시 알림 관리 API")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {
    private final DeviceRepository deviceRepository;
    private final FcmService fcmService;

    @Operation(summary = "기기 등록", description = "푸시 알림 수신을 위한 FCM 토큰 등록")
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody Device device) {
        if (deviceRepository.findByToken(device.getToken()).isEmpty()) {
            deviceRepository.save(device);
        }
        return ApiResponse.success("Registered");
    }

    @Operation(summary = "내 기기 목록 조회", description = "현재 사용자의 등록된 기기 목록 조회")
    @GetMapping
    public List<Device> getDevices() {
        return deviceRepository.findAll();
    }

    @Operation(summary = "테스트 푸시 발송", description = "현재 로그인된 사용자의 모든 기기로 테스트 푸시 알림 발송")
    @PostMapping("/test-push")
    public ResponseEntity<ApiResponse<String>> sendTestPush(@CurrentUser User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String loginId = user.getLoginId();
        List<Device> devices = deviceRepository.findByLoginId(loginId);
        
        if (devices.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("NO_DEVICE", "등록된 기기가 없습니다. 앱에서 먼저 로그인해주세요."));
        }

        int successCount = 0;
        for (Device device : devices) {
            try {
                fcmService.sendMessage(device.getToken(), "SSDMA 테스트 알림", "푸시 알림이 정상적으로 작동하고 있습니다!");
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send test push to device: {}", device.getToken(), e);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(String.format("%d개의 기기로 테스트 푸시를 발송했습니다.", successCount)));
    }
}
