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

    public static int decodeSymbol(char character) {
        if (!isTransportSymbol(character)) {
            throw new IllegalArgumentException("character is not a transport symbol");
        }

        return character - ALPHABET_START;
    }

    public static String encodeBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }

        if (bytes.length > ALPHABET_SIZE - 1) {
            throw new IllegalArgumentException("cannot encode more than 4095 bytes");
        }

        int dataSymbolCount = (bytes.length * 8 + 11) / 12;
        StringBuilder encoded = new StringBuilder(1 + dataSymbolCount);
        encoded.append(encodeSymbol(bytes.length));

        for (int index = 0; index < bytes.length; index += 3) {
            int firstByte = bytes[index] & 0xFF;
            int firstSymbol = firstByte << 4;

            if (index + 1 < bytes.length) {
                int secondByte = bytes[index + 1] & 0xFF;
                firstSymbol |= secondByte >>> 4;
            }

            encoded.append(encodeSymbol(firstSymbol));

            if (index + 1 < bytes.length) {
                int secondByte = bytes[index + 1] & 0xFF;
                int secondSymbol = (secondByte & 0x0F) << 8;

                if (index + 2 < bytes.length) {
                    int thirdByte = bytes[index + 2] & 0xFF;
                    secondSymbol |= thirdByte;
                }

                encoded.append(encodeSymbol(secondSymbol));
            }
        }

        return encoded.toString();
    }

    public static byte[] decodeBytes(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("encoded text cannot be null");
        }

        if (encoded.isEmpty()) {
            throw new IllegalArgumentException("encoded text is missing its length symbol");
        }

        int byteLength = decodeSymbol(encoded.charAt(0));
        int expectedDataSymbols = (byteLength * 8 + 11) / 12;

        if (encoded.length() != 1 + expectedDataSymbols) {
            throw new IllegalArgumentException("encoded text has the wrong number of symbols");
        }

        byte[] decoded = new byte[byteLength];
        int symbolIndex = 1;

        for (int byteIndex = 0; byteIndex < byteLength; byteIndex += 3) {
            int firstSymbol = decodeSymbol(encoded.charAt(symbolIndex++));

            decoded[byteIndex] = (byte) (firstSymbol >>> 4);

            if (byteIndex + 1 >= byteLength) {
                if ((firstSymbol & 0x0F) != 0) {
                    throw new IllegalArgumentException("encoded text has invalid padding");
                }

                continue;
            }

            int secondSymbol = decodeSymbol(encoded.charAt(symbolIndex++));
            decoded[byteIndex + 1] = (byte)(
                ((firstSymbol & 0x0F) << 4) | (secondSymbol >>> 8)
            );

            if (byteIndex + 2 < byteLength) {
                decoded[byteIndex + 2] = (byte) (secondSymbol & 0xFF);
            }  else if ((secondSymbol & 0xFF) != 0) {
                throw new IllegalArgumentException("encoded text has invalid padding");
            }
        }

        return decoded;
    }

    public static boolean isTransportSymbol(char character) {
        return character >= ALPHABET_START && character < ALPHABET_START + ALPHABET_SIZE;
    }
}