package com.cewko.unkrypt.crypto;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class AesGcmCipher {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = 16;
    private final SecureRandom random = new SecureRandom();

    public byte[] encrypt(SecretKey key, byte[] plaintext)
        throws GeneralSecurityException {
        
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext cannot be null");
        }

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        random.nextBytes(nonce);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key,
            new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
        );

        byte[] ciphertextAndTag = cipher.doFinal(plaintext);
        byte[] payload = new byte[nonce.length + ciphertextAndTag.length];

        System.arraycopy(
            nonce,
            0,
            payload,
            0,
            nonce.length
        );

        System.arraycopy(
            ciphertextAndTag,
            0,
            payload,
            nonce.length,
            ciphertextAndTag.length
        );

        return payload;
    }

    public byte[] decrypt(SecretKey key, byte[] payload)
        throws GeneralSecurityException {
        
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }

        int minimumPayloadLength = NONCE_LENGTH_BYTES + TAG_LENGTH_BYTES;

        if (payload.length < minimumPayloadLength) {
            throw new IllegalArgumentException("encrypted payload is too short");
        }

        byte[] nonce = Arrays.copyOfRange(
            payload,
            0,
            NONCE_LENGTH_BYTES
        );

        byte [] ciphertextAndTag = Arrays.copyOfRange(
            payload,
            NONCE_LENGTH_BYTES,
            payload.length
        );

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
        );

        return cipher.doFinal(ciphertextAndTag);
    }
}