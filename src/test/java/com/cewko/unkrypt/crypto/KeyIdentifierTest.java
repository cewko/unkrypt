package com.cewko.unkrypt.crypto;

import static org.junit.Assert.assertEquals;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

public final class KeyIdentifierTest {
    @Test
    public void derivesKnownIdentifier() throws Exception {
        byte[] keyBytes = new byte[16];

        for (int index = 0; index < keyBytes.length; index++) {
            keyBytes[index] = (byte) index;
        }

        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        assertEquals(0xC18DF7, KeyIdentifier.derive(key));
    }

    @Test
    public void producesSameIdentifierForSameKey() throws Exception {
        SecretKey key = new SecretKeySpec(new byte[16], "AES");

        int first = KeyIdentifier.derive(key);
        int second = KeyIdentifier.derive(key);

        assertEquals(first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNon128BitKey() throws Exception {
        SecretKey wrongLengthKey = new SecretKeySpec(new byte[8], "AES");
        KeyIdentifier.derive(wrongLengthKey);
    }
}