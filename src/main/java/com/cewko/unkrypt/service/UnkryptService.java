package com.cewko.unkrypt.service;

import com.cewko.unkrypt.crypto.AesGcmCipher;
import com.cewko.unkrypt.crypto.KeyIdentifier;
import com.cewko.unkrypt.transport.TransportEnvelope;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

public final class UnkryptService {
    private final AesGcmCipher cipher = new AesGcmCipher();

    public String encrypt(SecretKey key, String plaintext) 
        throws GeneralSecurityException {

        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext cannot be null");
        }

        if (plaintext.isEmpty()) {
            throw new IllegalArgumentException("plaintext cannot be empty");
        }

        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        int keyIdentifier = KeyIdentifier.derive(key);
        byte[] encryptedPayload = cipher.encrypt(key, plaintextBytes);
        
        return TransportEnvelope.encode(keyIdentifier, encryptedPayload);
    }

    public String decrypt(SecretKey key, String encodedMessage)
        throws GeneralSecurityException {

        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        if (encodedMessage == null) {
            throw new IllegalArgumentException("encoded message cannot be null");
        }

        TransportEnvelope envelope = TransportEnvelope.decode(encodedMessage);
        int expectedKeyIdentifier = KeyIdentifier.derive(key);

        if (envelope.getKeyIdentifier() != expectedKeyIdentifier) {
            throw new IllegalArgumentException("message uses a different shared key");
        }

        byte[] plaintextBytes = cipher.decrypt(key, envelope.getEncryptedPayload());
        
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }
}