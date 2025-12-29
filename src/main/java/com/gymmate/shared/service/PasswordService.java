package com.gymmate.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling password encryption and validation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Encode a plain text password.
     */
    public String encode(String plainPassword) {
        // logging the password encoding process
        log.debug("Encoding password");
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify if a plain text password matches the encoded password.
     */
    public boolean matches(String plainPassword, String encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword);
    }
}
