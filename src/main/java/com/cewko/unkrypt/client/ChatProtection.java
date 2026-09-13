package com.cewko.unkrypt.client;

import com.cewko.unkrypt.service.UnkryptService;
import com.cewko.unkrypt.state.UnkryptSession;
import java.security.GeneralSecurityException;
import net.minecraft.client.Minecraft;

public final class ChatProtection {

    public interface Sender {
        void sendNormally(String message, boolean addToHistory);
    }

    private final UnkryptSession session;
    private final UnkryptService service;

    public ChatProtection(UnkryptSession session, UnkryptService service) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }

        if (service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }

        this.session = session;
        this.service = service;
    }

    public boolean shouldBlockAutocomplete(String draft) {
        return session.isEncryptionEnabled() && !draft.trim().startsWith("/");
    }

    public void send(String message, boolean addToHistory, Sender sender) {
        if (!session.isEncryptionEnabled() || message.startsWith("/")) {
            sender.sendNormally(message, addToHistory);
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();

        if (addToHistory) {
            minecraft.ingameGUI.getChatGUI().addToSentMessages(message);
        }

        try {
            String encryptedMessage = service.encrypt(
                session.getSharedKey(),
                message
            );
            sender.sendNormally(encryptedMessage, false);
        } catch (IllegalArgumentException exception) {
            UnkryptMessages.error(exception.getMessage());
        } catch (GeneralSecurityException exception) {
            UnkryptMessages.error("could not encrypt message");
        }
    }
}
