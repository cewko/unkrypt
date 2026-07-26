package com.cewko.unkrypt.service;

import static org.junit.Assert.assertEquals;

import com.cewko.unkrypt.crypto.SharedKeyCodec;
import com.cewko.unkrypt.transport.TransportEnvelope;

import javax.crypto.SecretKey;

import org.junit.Test;

public final class UnkryptServiceTest {
    private final SharedKeyCodec keyCodec = new SharedKeyCodec();
    private final UnkryptService service = new UnkryptService();

    @Test
    public void roundTripsNormalText() throws Exception {
        SecretKey key = keyCodec.generate();
        String encoded = service.encrypt(key, "how often do you meow?");
        String decoded = service.decrypt(key, encoded);

        assertEquals("how often do you meow?", decoded);
    }

    @Test
    public void roundTripsMultilingualText() throws Exception {
        SecretKey key = keyCodec.generate();
        String original = "Polish: Za\u017C\u00F3\u0142\u0107; "
            + "Japanese: \u65E5\u672C\u8A9E; "
            + "Emoji: \uD83D\uDE00";

        String encoded = service.encrypt(key, original);
        String decoded = service.decrypt(key, encoded);

        assertEquals(original, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDifferentKeyIdentifier() throws Exception {
        SecretKey key = keyCodec.generate();
        String encoded = service.encrypt(key, "hello");
        TransportEnvelope original = TransportEnvelope.decode(encoded);

        int differentIdentifier = original.getKeyIdentifier() ^ 1;
        String changed = TransportEnvelope.encode(
            differentIdentifier,
            original.getEncryptedPayload()
        );

        service.decrypt(key, changed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMessageWithoutEnvelope() throws Exception {
        SecretKey key = keyCodec.generate();
        service.decrypt(key, "ordinary message");
    }

    @Test
    public void acceptsLargestJapaneseMessage() throws Exception {
        SecretKey key = keyCodec.generate();
        String original = repeatJapaneseCharacter(38);
        String encoded = service.encrypt(key, original);

        assertEquals(100, encoded.length());
        assertEquals(original, service.decrypt(key, encoded));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOversizedJapaneseMessage() throws Exception {
        SecretKey key = keyCodec.generate();
        String oversized = repeatJapaneseCharacter(39);

        service.encrypt(key, oversized);
    }

    private String repeatJapaneseCharacter(int count) {
        StringBuilder result = new StringBuilder(count);

        for (int index = 0; index < count; index++) {
            result.append('\u65E5');
        }

        return result.toString();
    }
}