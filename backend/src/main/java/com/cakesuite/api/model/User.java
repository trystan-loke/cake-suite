package com.cakesuite.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;  // This is the Firebase UID
    
    private String email;
    private String displayName;
    private String photoUrl;
    
    // Additional user metadata that's not stored in Firebase
    private String role;
}
