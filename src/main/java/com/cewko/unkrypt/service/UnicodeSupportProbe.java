package com.cewko.unkrypt.service;

import com.cewko.unkrypt.transport.UnicodeTransport;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public final class UnicodeSupportProbe {
    private static final String PROBE_MARKER  = "~u1:";
    private static final int MAX_MESSAGE_LENGTH = 100;

    private static final long TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final SecureRandom random = new SecureRandom();

    private Status status = Status.NOT_CHECKED;
    private String identifier;
    private String expectedPayload;
    private long deadlineNanos;

    public String start(long currentTimeNanos) {
        identifier = createIdentifier();
        expectedPayload = UnicodeTransport.createProbePayload(random);
        deadlineNanos = currentTimeNanos + TIMEOUT_NANOS;
        status = Status.WAITING;

        String message = PROBE_MARKER + identifier + ":" + expectedPayload + ":" + identifier;

        if (message.length() > MAX_MESSAGE_LENGTH) {
            status = Status.MODIFIED;

            throw new IllegalStateException(
                "unicode probe exceeds Minecraft's message length limit"
            );
        }

        return message;
    }

    public boolean inspectIncoming(String incomingMessage) {
        if (status != Status.WAITING || incomingMessage == null) {
            return false;
        }

        String prefix = PROBE_MARKER + identifier + ":";
        int prefixPosition = incomingMessage.indexOf(prefix);

        if (prefixPosition < 0) {
            return false;
        }

        int payloadStart = prefixPosition + prefix.length();
        String suffix = ":" + identifier;
        int payloadEnd = incomingMessage.indexOf(suffix, payloadStart);

        if (payloadEnd < 0) {
            status = Status.MODIFIED;
            return true;
        }

        String returnedPayload = incomingMessage.substring(payloadStart, payloadEnd);

        if (expectedPayload.equals(returnedPayload)) {
            status = Status.SUPPORTED;
        } else {
            status = Status.MODIFIED;
        }

        return true;
    }

    public void updateTimeout(long currentTimeNanos) {
        if (
            status == Status.WAITING
            && currentTimeNanos - deadlineNanos >= 0
        ) {
            status = Status.TIMED_OUT;
        }
    }

    public boolean isRunning() {
        return status == Status.WAITING;
    }

    public String getStatusMessage() {
        switch (status) {
            case WAITING:
                return "Checking Unicode support...";

            case SUPPORTED:
                return "Unicode supported";

            case MODIFIED:
                return "Unicode was modified";

            case TIMED_OUT:
                return "Timed out";

            case NOT_CHECKED:
            default:
                return "Unicode support has not been checked";
        }
    }

    private String createIdentifier() {
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);


        StringBuilder result = new StringBuilder(8);

        for (byte value : bytes) {
            int unsignedValue = value & 0xFF;

            result.append(HEX[unsignedValue >>> 4]);
            result.append(HEX[unsignedValue & 0x0F]);
        }

        return result.toString();
    }

    public void reset() {
        status = Status.NOT_CHECKED;
        identifier = null;
        expectedPayload = null;
        deadlineNanos = 0L;
    }

    private enum Status {
        NOT_CHECKED,
        WAITING,
        SUPPORTED,
        MODIFIED,
        TIMED_OUT
    }
}