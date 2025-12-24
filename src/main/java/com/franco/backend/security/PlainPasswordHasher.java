package com.franco.backend.security;

import org.springframework.stereotype.Component;

//@Component
public class PlainPasswordHasher implements PasswordService {

    // Just for testing purposes. Do NOT use in production.
    @Override
    public String hash(String rawPassword) {
        return rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword.equals(hashedPassword);
    }
}
