package com.cewko.unkrypt.transport;

import java.security.SecureRandom;

public final class UnicodeTransport {
    public static final int ALPHABET_START = 0x4E00;
    public static final int ALPHABET_SIZE = 4096;

    private static final int[] PROBE_SYMBOLS = {
        0x000, 0x001, 0x00F, 0x07F,
        0x080, 0x0FF, 0x100, 0x3FF,
        0x400, 0x7FF, 0x800, 0xBFF,
        0xC00, 0xEFF, 0xFFE, 0xFFF
    };

    private UnicodeTransport() {
    }

    public static String createProbePayload(SecureRandom random) {
        if (random == null) {
            throw new IllegalArgumentException("random cannot be null");
        }

        StringBuilder payload = new StringBuilder();

        for (int symbol : PROBE_SYMBOLS) {
            payload.append(encodeSymbol(symbol));
        }

        for (int i = 0; i < 8; i++) {
            int randomPosition = random.nextInt(ALPHABET_SIZE);
            char unicodeSymbol = encodeSymbol(randomPosition);
            payload.append(unicodeSymbol);
        }

        return payload.toString();
    }

    public static char encodeSymbol(int value) {
        if (value < 0 || value >= ALPHABET_SIZE) {
            throw new IllegalArgumentException(
                "transport value must be between 0 and 4095"
            );
        }
        return (char) (ALPHABET_START + value);
    }

    public static boolean isTransportSymbol(char character) {
        return character >= ALPHABET_START && character < ALPHABET_START + ALPHABET_SIZE;
    }
}