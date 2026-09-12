package com.cewko.unkrypt.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiSleepMP;
import org.lwjgl.input.Keyboard;

public final class EncryptingGuiSleepMP
    extends GuiSleepMP
    implements ChatProtection.Sender
{

    private final ChatProtection protection;

    public EncryptingGuiSleepMP(ChatProtection protection) {
        this.protection = protection;
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode)
        throws IOException {
        if (
            keyCode == Keyboard.KEY_TAB &&
            protection.shouldBlockAutocomplete(inputField.getText())
        ) {
            return;
        }

        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    public void sendChatMessage(String message, boolean addToHistory) {
        protection.send(message, addToHistory, this);
    }

    @Override
    public void sendNormally(String message, boolean addToHistory) {
        super.sendChatMessage(message, addToHistory);
    }
}
