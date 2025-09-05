package com.cakesuite.api.config;

import com.google.auth.oauth2.GoogleCredentials;
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
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            if (firebaseCredentialsJson == null || firebaseCredentialsJson.trim().isEmpty()) {
                throw new IllegalStateException("Firebase credentials not provided. Set the FIREBASE_CREDENTIALS_JSON environment variable.");
            }

            // Decode if base64 encoded (recommended for newline handling)
            byte[] decodedCredentials;
            try {
                decodedCredentials = Base64.getDecoder().decode(firebaseCredentialsJson);
            } catch (IllegalArgumentException e) {
                // If not base64 encoded, use as is (plain JSON string)
                decodedCredentials = firebaseCredentialsJson.getBytes();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decodedCredentials)))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
