package com.cewko.unkrypt.transport;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.junit.Test;

public final class UnicodeTransportTest {

    @Test
    public void packsThreeBytesIntoTwoDataSymbols() {
        byte[] bytes = {
            (byte) 0xAB,
            (byte) 0xCD,
            (byte) 0xEF
        };

        String encoded = UnicodeTransport.encodeBytes(bytes);

        assertEquals(3, UnicodeTransport.decodeSymbol(encoded.charAt(0)));
        assertEquals(0xABC, UnicodeTransport.decodeSymbol(encoded.charAt(1)));
        assertEquals(0xDEF, UnicodeTransport.decodeSymbol(encoded.charAt(2)));
    }

    @Test
    public void roundTripsDifferentByteLengths() {
        Random random = new Random(123456789L);

        for (int length = 0; length <= 300; length++) {
            byte[] original = new byte[length];
            random.nextBytes(original);

            String encoded = UnicodeTransport.encodeBytes(original);
            byte[] decoded = UnicodeTransport.decodeBytes(encoded);

            assertArrayEquals(original, decoded);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingLengthSymbol() {
        UnicodeTransport.decodeBytes("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonTransportCharacters() {
        String malformed = new String(new char[] {
            UnicodeTransport.encodeSymbol(1),
            'A'
        });

        UnicodeTransport.decodeBytes(malformed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsWrongSymbolCount() {
        String encoded = UnicodeTransport.encodeBytes(new byte[] { 1, 2, 3 });
        UnicodeTransport.decodeBytes(encoded.substring(0, encoded.length() - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonZeroPadding() {
        String malformed = new String(new char[] {
            UnicodeTransport.encodeSymbol(1),
            UnicodeTransport.encodeSymbol(1)
        });

        UnicodeTransport.decodeBytes(malformed);
    }
}