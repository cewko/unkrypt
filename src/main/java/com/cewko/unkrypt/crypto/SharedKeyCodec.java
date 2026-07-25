package com.cewko.unkrypt.crypto;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class SharedKeyCodec {
    private static final String PREFIX = "uk1_";
    private static final int KEY_LENGTH_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    public SecretKey generate() {
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        random.nextBytes(keyBytes);

        try {
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    public String format(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        byte[] keyBytes = key.getEncoded();

        if (keyBytes == null) {
            throw new IllegalArgumentException("key does not expose encoded bytes");
        }

        try {
            if (keyBytes.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException("key must contain exactly 16 bytes");
            }

            String encoded = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(keyBytes);

            return PREFIX + encoded;
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    public SecretKey parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("key text cannot be null");
        }

        String cleanedText = text.trim();

        if (!cleanedText.startsWith(PREFIX)) {
            throw new IllegalArgumentException("key must begin with " + PREFIX);
        }

        String encodedPart = cleanedText.substring(PREFIX.length());
        byte [] keyBytes;

        try {
            keyBytes = Base64.getUrlDecoder().decode(encodedPart);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("key contains invalid Base64", exception);
        }


        try {
            if (keyBytes.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException("key must contain exactly 16 bytes");
            }
            
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
}