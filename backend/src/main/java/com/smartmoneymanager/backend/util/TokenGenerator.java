package com.smartmoneymanager.backend.util;

import java.security.SecureRandom;
import java.util.Base64;

/** Generates cryptographically random, URL-safe, single-use tokens (email verification / password reset). */
public final class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
