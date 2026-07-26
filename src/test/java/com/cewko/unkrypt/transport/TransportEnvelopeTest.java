package com.cewko.unkrypt.transport;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TransportEnvelopeTest {
    @Test
    public void roundTripsIdentifierAndPayload() {
        int identifier = 0xABCDEF;
        byte[] payload = { 1, 2, 3, 4, 5 };

        String encoded = TransportEnvelope.encode(identifier, payload);
        TransportEnvelope decoded = TransportEnvelope.decode(encoded);

        assertEquals(identifier, decoded.getKeyIdentifier());
        assertArrayEquals(payload, decoded.getEncryptedPayload());
    }

    @Test
    public void storesIdentifierAsTwoSymbols() {
        String encoded = TransportEnvelope.encode(0xABCDEF, new byte[] { 1 });

        assertTrue(encoded.startsWith("1U"));
        assertEquals(
            0xABC, UnicodeTransport.decodeSymbol(encoded.charAt(2))
        );
        assertEquals(
            0xDEF, UnicodeTransport.decodeSymbol(encoded.charAt(3))
        );
    }

    @Test
    public void acceptsExactlyOneHundredCharacters() {
        String encoded = TransportEnvelope.encode(0x123456, new byte[142]);
        assertEquals(100, encoded.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMessagesOverOneHundredCharacters() {
        TransportEnvelope.encode(0x123456, new byte[143]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsIdentifierLargerThanTwentyFourBits() {
        TransportEnvelope.encode(0x1000000, new byte[] { 1 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsWrongMarker() {
        String valid = TransportEnvelope.encode(0x123456, new byte[] { 1, 2, 3 });
        String wrongMarker = "XX" + valid.substring(2);
        TransportEnvelope.decode(wrongMarker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBrokenPackedPayload() {
        String malformed =
            "1U"
            + UnicodeTransport.encodeSymbol(0)
            + UnicodeTransport.encodeSymbol(0)
            + "A";

        TransportEnvelope.decode(malformed);
    }
}