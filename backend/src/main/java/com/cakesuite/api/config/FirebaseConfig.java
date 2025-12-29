package com.cakesuite.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS_JSON}")
    private String firebaseCredentialsJson;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        if (firebaseCredentialsJson == null || firebaseCredentialsJson.trim().isEmpty()) {
            throw new IllegalStateException("Set FIREBASE_CREDENTIALS_JSON env var.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(firebaseCredentialsJson);
        } catch (IllegalArgumentException e) {
            decoded = firebaseCredentialsJson.getBytes();
        }
        return GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
    }

    @Bean
    public FirebaseApp firebaseApp(GoogleCredentials creds) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public Storage storage(GoogleCredentials creds) {
        return StorageOptions.newBuilder()
                .setCredentials(creds)
                .build()
                .getService();
    }
}
