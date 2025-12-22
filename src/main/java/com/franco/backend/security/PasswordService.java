package com.franco.backend.security;

public interface PasswordService {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}

