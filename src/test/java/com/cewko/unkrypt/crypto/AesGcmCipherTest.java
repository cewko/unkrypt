package com.cewko.unkrypt.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.junit.Test;

public final class AesGcmCipherTest {
    private final SharedKeyCodec keyCodec = new SharedKeyCodec();
    private final AesGcmCipher cipher = new AesGcmCipher();

    @Test
    public void roundTripsMultilingualUtf8() throws Exception {
        String original = "Polish: Za\u017C\u00F3\u0142\u0107 "
            + "g\u0119\u015Bl\u0105 ja\u017A\u0144; "
            + "Cyrillic: \u041F\u0440\u0438\u0432\u0435\u0442; "
            + "Arabic: \u0645\u0631\u062D\u0628\u0627; "
            + "Chinese: \u4F60\u597D; "
            + "Emoji: \uD83D\uDE00";

        SecretKey key = keyCodec.generate();

        byte[] encrypted = cipher.encrypt(
            key,
            original.getBytes(StandardCharsets.UTF_8)
        );

        byte[] decrypted = cipher.decrypt(key, encrypted);
        assertEquals(original, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void addsNonceAndAuthenticationTag() throws Exception {
        SecretKey key = keyCodec.generate();
        byte[] plaintext = { 1, 2, 3 };
        byte[] encrypted = cipher.encrypt(key, plaintext);
        assertEquals(plaintext.length + 12 + 16, encrypted.length);
    }

    @Test
    public void usesFreshNonceForEveryMessage() throws Exception {
        SecretKey key = keyCodec.generate();
        byte[] plaintext = { 1, 2, 3 };
        byte[] first = cipher.encrypt(key, plaintext);
        byte[] second = cipher.encrypt(key, plaintext);
        assertFalse(Arrays.equals(first, second));
    }

    @Test(expected = GeneralSecurityException.class)
    public void rejectsWrongKey() throws Exception {
        SecretKey correctKey = keyCodec.generate();
        SecretKey wrongKey = keyCodec.generate();

        byte[] encrypted = cipher.encrypt(correctKey, new byte[] { 1, 2, 3 });
        cipher.decrypt(wrongKey, encrypted);
    }

    @Test(expected = GeneralSecurityException.class)
    public void rejectsModifiedPayload() throws Exception {
        SecretKey key = keyCodec.generate();
        byte[] encrypted = cipher.encrypt(key, new byte[] { 1, 2, 3 });
        encrypted[encrypted.length - 1] ^= 1;
        cipher.decrypt(key, encrypted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTruncatedPayload() throws Exception {
        SecretKey key = keyCodec.generate();
        cipher.decrypt(key, new byte[27]);
    }
}