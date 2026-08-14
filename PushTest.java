import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.FileInputStream;

public class PushTest {
    public static void main(String[] args) throws Exception {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/smsmonitorweb-firebase.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        String token = "dB6lfTbjTiSpcLL68fPPD5:APA91bEcq_FqNkwVSRiIC-oJ2uvzgGanSnBSgl110kPEsfq3bZr3rsSGOYJJXTBnA7kRjIvBMYR4-QC-Rv-B-iWGp7BnDx3WOgC98-NdElX6Rhl70HXYeNI";
        
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("최종 테스트 알림")
                        .setBody("직접 실행한 Java 코드로 보낸 푸시입니다!")
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + response);
    }
}
