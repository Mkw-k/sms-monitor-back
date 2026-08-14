package com.mk.www.smsmonitor.common.application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;

public class FcmServiceTest {

    @BeforeAll
    static void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new ClassPathResource("smsmonitorweb-firebase.json").getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    @Test
    
    void sendPushTest() {
        FcmService fcmService = new FcmService();
        // 여기에 앱에서 받은 토큰을 넣고 실행
        String token = "dB6lfTbjTiSpcLL68fPPD5:APA91bEcq_FqNkwVSRiIC-oJ2uvzgGanSnBSgl110kPEsfq3bZr3rsSGOYJJXTBnA7kRjIvBMYR4-QC-Rv-B-iWGp7BnDx3WOgC98-NdElX6Rhl70HXYeNI";
        fcmService.sendMessage(token, "테스트", "백엔드 JUnit 테스트입니다!");
    }
}
