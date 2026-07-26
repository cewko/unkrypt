package com.cewko.unkrypt.client;

import com.cewko.unkrypt.service.UnkryptService;
import com.cewko.unkrypt.state.UnkryptSession;
import com.cewko.unkrypt.transport.TransportEnvelope;

import java.security.GeneralSecurityException;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public final class EncryptingGuiChat extends GuiChat {
    private final UnkryptSession session;
    private final UnkryptService service;

    public EncryptingGuiChat(
        String defaultText, UnkryptSession session, UnkryptService service
    ) {
        super(defaultText);

        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }

        if (service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }

        this.session = session;
        this.service = service;
    }

    @Override
    public void sendChatMessage(String message, boolean addToHistory) {
        if (
            !session.isEncryptionEnabled()
            || message.startsWith("/")
            || TransportEnvelope.startsWithMarker(message)
        ) {
            super.sendChatMessage(message, addToHistory);
            return;
        }

        if (addToHistory) {
            mc.ingameGUI.getChatGUI().addToSentMessages(message);
        }

        try {
            String encryptedMessage = service.encrypt(
                session.getSharedKey(),
                message
            );

            super.sendChatMessage(encryptedMessage, false);
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        } catch (GeneralSecurityException exception) {
            showError("something went wrong");
        }
    }

    private void showError(String message) {
        ChatComponentText error = new ChatComponentText(
            EnumChatFormatting.RED
            + "[unkrypt] "
            + message
        );

        mc.ingameGUI.getChatGUI().printChatMessage(error);
    }
}