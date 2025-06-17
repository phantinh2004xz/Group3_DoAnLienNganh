package com.yourcompany.onlineexam.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.sdk.path}") // Lấy đường dẫn từ application.properties
    private String firebaseSdkPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initializeFirebaseApp() throws IOException {
        try {
            if (FirebaseApp.getApps().isEmpty()) { // Kiểm tra để tránh khởi tạo nhiều lần
                Resource resource = resourceLoader.getResource(firebaseSdkPath);
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK đã được khởi tạo thành công!");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi khởi tạo Firebase Admin SDK: " + e.getMessage());
            throw new IOException("Không thể khởi tạo Firebase Admin SDK", e);
        }
    }

    // Tạo Bean cho Firestore để có thể inject vào các service khác
    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}