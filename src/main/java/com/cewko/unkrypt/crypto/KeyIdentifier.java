package com.cewko.unkrypt.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class KeyIdentifier {
    private static final String ALGORITHM = "HmacSHA256";
    private static final byte[] CONTEXT = 
        "Unkrypt/key-identifier/v1".getBytes(StandardCharsets.UTF_8);
    private static final int AES_KEY_LENGTH_BYTES = 16;

    private KeyIdentifier() {
    }

    public static int derive(SecretKey key) throws GeneralSecurityException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        byte[] keyBytes = key.getEncoded();

        if (keyBytes == null) {
            throw new IllegalArgumentException("key doesn't expose encoded bytes");
        }

        try {
            if (keyBytes.length != AES_KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException("key must contain exactly 16 bytes");
            }

            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(keyBytes, ALGORITHM));
            byte [] digest = mac.doFinal(CONTEXT);

            try {
                int firstByte = digest[0] & 0xFF;
                int secondByte = digest[1] & 0xFF;
                int thirdByte = digest[2] & 0xFF;

                int identifier = (firstByte << 16) | (secondByte << 8) | thirdByte;
                return identifier;
            } finally {
                Arrays.fill(digest, (byte) 0);
            }
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
}