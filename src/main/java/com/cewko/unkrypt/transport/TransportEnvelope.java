package com.cewko.unkrypt.transport;

public final class TransportEnvelope {
    private static final String MARKER = "1U";

    private static final int MAX_KEY_IDENTIFIER = 0xFFFFFF;
    private static final int MAX_MESSAGE_LENGTH = 100;
    private static final int KEY_IDENTIFIER_SYMBOLS = 2;

    private static final int MINIMUM_MESSAGE_LENGTH = 
        MARKER.length() + KEY_IDENTIFIER_SYMBOLS + 1;

    private final int keyIdentifier;
    private final byte[] encryptedPayload;

    private TransportEnvelope(int keyIdentifier, byte[] encryptedPayload) {
        this.keyIdentifier = keyIdentifier;
        this.encryptedPayload = encryptedPayload.clone();
    }

    public int getKeyIdentifier() {
        return keyIdentifier;
    }

    public byte[] getEncryptedPayload() {
        return encryptedPayload.clone();
    }

    public static String encode(int keyIdentifier, byte[] encryptedPayload) {
        if (keyIdentifier < 0 || keyIdentifier > MAX_KEY_IDENTIFIER) {
            throw new IllegalArgumentException(
                "key identifier must be between 0 and 0xFFFFFF"
            );
        }

        if (encryptedPayload == null) {
            throw new IllegalArgumentException("encrypted payload cannot be null");
        }

        int firstIdentifierPart = (keyIdentifier >>> 12) & 0xFFF;
        int secondIdentifierPart = keyIdentifier & 0xFFF;

        String packedPayload = UnicodeTransport.encodeBytes(encryptedPayload);
        StringBuilder message = new StringBuilder();
        message.append(MARKER);
        message.append(UnicodeTransport.encodeSymbol(firstIdentifierPart));
        message.append(UnicodeTransport.encodeSymbol(secondIdentifierPart));
        message.append(packedPayload);

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                "encrypted message exceeds 100 characters"
            );
        }

        return message.toString();
    }

    public static TransportEnvelope decode(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                "encrypted message exceeds 100 characters"
            );
        }

        if (message.length() < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("encrypted message is incomplete");
        }

        if (!message.startsWith(MARKER)) {
            throw new IllegalArgumentException("message has no unkrypt marker");
        }

        int firstIdentifierPart = UnicodeTransport.decodeSymbol(
            message.charAt(MARKER.length())
        );
        int secondIdentifierPart = UnicodeTransport.decodeSymbol(
            message.charAt(MARKER.length() + 1)
        );

        int keyIdentifier = (firstIdentifierPart << 12) | secondIdentifierPart;

        String packedPayload = message.substring(
            MARKER.length() + KEY_IDENTIFIER_SYMBOLS
        );
        byte[] encryptedPayload = UnicodeTransport.decodeBytes(packedPayload);

        return new TransportEnvelope(keyIdentifier, encryptedPayload);
    }

    public static boolean startsWithMarker(String message) {
        return message != null && message.startsWith(MARKER);
    }
}