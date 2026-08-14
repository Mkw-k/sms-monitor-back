package com.mk.www.smsmonitor.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("smsmonitorweb-firebase.json");
            if (!resource.exists()) {
                log.warn("Firebase credential file (smsmonitorweb-firebase.json) not found in classpath. Firebase initialization skipped.");
                return;
            }

            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Hot-reload 대응: 이미 초기화된 앱이 있으면 삭제 후 재설정
            if (!FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.getInstance().delete();
                log.info("Existing Firebase app deleted for re-initialization");
            }

            FirebaseApp.initializeApp(options);
            log.info("Firebase application has been initialized. Project: {}", FirebaseApp.getInstance().getOptions().getProjectId());
            
            // [테스트] 서버 시작 즉시 푸시 발송
            sendTestPush();
        } catch (Exception e) {
            log.error("Firebase initialization error: {} ", e.getMessage());
        }
    }

    private void sendTestPush() {
        String token = "dB6lfTbjTiSpcLL68fPPD5:APA91bEcq_FqNkwVSRiIC-oJ2uvzgGanSnBSgl110kPEsfq3bZr3rsSGOYJJXTBnA7kRjIvBMYR4-QC-Rv-B-iWGp7BnDx3WOgC98-NdElX6Rhl70HXYeNI";
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("서버 기동 알림")
                            .setBody("백엔드 서버가 시작되어 자동으로 보낸 푸시입니다!")
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
            log.info("Test push sent successfully on startup");
        } catch (Exception e) {
            log.error("Test push failed: {} ", e.getMessage());
        }
    }
}
