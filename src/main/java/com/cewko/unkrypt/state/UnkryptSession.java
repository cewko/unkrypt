package com.cewko.unkrypt.state;

import javax.crypto.SecretKey;

public final class UnkryptSession {
    private boolean encryptionEnabled = false;
    private boolean decryptionEnabled = false;

    private SecretKey sharedKey;

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public boolean isDecryptionEnabled() {
        return decryptionEnabled;
    }

    public void toggleEncryption() {
        encryptionEnabled = !encryptionEnabled;
    }

    public void toggleDecryption() {
        decryptionEnabled = !decryptionEnabled;
    }

    public boolean hasSharedKey() {
        return sharedKey != null;
    }

    public SecretKey getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(SecretKey sharedKey) {
        if (sharedKey == null) {
            throw new IllegalArgumentException("shared key cannot be null");
        }

        this.sharedKey = sharedKey;
    }
}
