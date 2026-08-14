package com.mk.www.smsmonitor.common.application;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendMessage(String targetToken, String title, String body) {
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp is not initialized. Skipping push notification to token: {}", targetToken);
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: {} ", response);
        } catch (Exception e) {
            log.error("Failed to send FCM message: {} ", e.getMessage());
        }
    }
}
