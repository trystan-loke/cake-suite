package com.cakesuite.api.security;

import com.cakesuite.api.model.User;
import com.cakesuite.api.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authorizationHeader.substring(7);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            
            // Load or create user in our system
            Optional<User> existingUser = userRepository.findById(uid);
            User user;
            
            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update user info if needed
                boolean needsUpdate = false;
                
                if (email != null && !email.equals(user.getEmail())) {
                    user.setEmail(email);
                    needsUpdate = true;
                }
                
                if (name != null && !name.equals(user.getDisplayName())) {
                    user.setDisplayName(name);
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    userRepository.save(user);
                }
            } else {
                // Create new user
                user = User.builder()
                        .id(uid)
                        .email(email)
                        .displayName(name)
                        .role("USER") // Default role
                        .build();
                userRepository.save(user);
            }
            
            // Set authentication
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
            }
            
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase Authentication failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
