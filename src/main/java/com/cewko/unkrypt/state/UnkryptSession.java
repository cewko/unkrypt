package com.cewko.unkrypt.state;

public final class UnkryptSession {
    private boolean encryptionEnabled = false;
    private boolean decryptionEnabled = false;

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
}
