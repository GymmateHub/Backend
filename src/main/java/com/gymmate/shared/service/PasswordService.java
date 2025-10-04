package com.gymmate.shared.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling password encryption and validation.
 */
@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Encode a plain text password.
     */
    public String encode(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Verify if a plain text password matches the encoded password.
     */
    public boolean matches(String plainPassword, String encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword);
    }
}