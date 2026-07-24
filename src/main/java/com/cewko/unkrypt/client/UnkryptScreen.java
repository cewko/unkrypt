package com.cewko.unkrypt.client;

import com.cewko.unkrypt.state.UnkryptSession;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;


public final class UnkryptScreen extends GuiScreen {
    private static final int ENCRYPT_BUTTON_ID = 1;
    private static final int DECRYPT_BUTTON_ID = 2;
    private static final int NEW_KEY_BUTTON_ID = 3;
    private static final int COPY_BUTTON_ID = 4;
    private static final int PASTE_BUTTON_ID = 5;
    private static final int SHOW_BUTTON_ID = 6;
    private static final int UNICODE_CHECK_BUTTON_ID = 7;
    private static final int DONE_BUTTON_ID = 8;

    private static final int CONTENT_WIDTH = 204;

    private final UnkryptSession session;

    private GuiButton encryptButton;
    private GuiButton decryptButton;
    private GuiTextField sharedKeyField;

    private int contentLeft;
    private int contentTop;

    public UnkryptScreen(UnkryptSession session) {
        this.session = session;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        contentLeft = (width - CONTENT_WIDTH) / 2;
        contentTop = height / 2 - 85;

        encryptButton = new GuiButton(
            ENCRYPT_BUTTON_ID,
            contentLeft,
            contentTop + 22,
            100,
            20,
            ""
        );

        decryptButton = new GuiButton(
            DECRYPT_BUTTON_ID,
            contentLeft + 104,
            contentTop + 22,
            100,
            20,
            ""
        );

        buttonList.add(encryptButton);
        buttonList.add(decryptButton);

        sharedKeyField = new GuiTextField(
            20,
            fontRendererObj,
            contentLeft,
            contentTop + 62,
            CONTENT_WIDTH,
            20
        );

        sharedKeyField.setMaxStringLength(64);
        sharedKeyField.setEnabled(false);

        addDisabledKeyButton(
            NEW_KEY_BUTTON_ID,
            contentLeft,
            contentTop + 88,
            "New"
        );

        addDisabledKeyButton(
            COPY_BUTTON_ID,
            contentLeft + 52,
            contentTop + 88,
            "Copy"
        );

        addDisabledKeyButton(
            PASTE_BUTTON_ID,
            contentLeft + 104,
            contentTop + 88,
            "Paste"
        );

        addDisabledKeyButton(
            SHOW_BUTTON_ID,
            contentLeft + 156,
            contentTop + 88,
            "Show"
        );

        GuiButton unicodeCheckButton = new GuiButton(
            UNICODE_CHECK_BUTTON_ID,
            contentLeft,
            contentTop + 114,
            CONTENT_WIDTH,
            20,
            "Check Unicode support"
        );

        unicodeCheckButton.enabled = false;
        buttonList.add(unicodeCheckButton);

        buttonList.add(new GuiButton(
            DONE_BUTTON_ID,
            contentLeft,
            contentTop + 150,
            CONTENT_WIDTH,
            20,
            "Done"
        ));

        updateToggleLabels();
    }

    private void addDisabledKeyButton(
        int id,
        int x,
        int y,
        String label
    ) {
        GuiButton button = new GuiButton(id, x, y, 48, 20, label);
        button.enabled = false;
        buttonList.add(button);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ENCRYPT_BUTTON_ID:
                session.toggleEncryption();
                updateToggleLabels();
                break;

            case DECRYPT_BUTTON_ID:
                session.toggleDecryption();
                updateToggleLabels();
                break;

            case DONE_BUTTON_ID:
                mc.displayGuiScreen(null);
                break;

            default:
                break;
        }
    }

    private void updateToggleLabels() {
        encryptButton.displayString =
            "Encrypt: " + onOrOff(session.isEncryptionEnabled());

        decryptButton.displayString =
            "Decrypt: " + onOrOff(session.isDecryptionEnabled());
    }

    private String onOrOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    @Override
    public void updateScreen() {
        sharedKeyField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode)
        throws IOException {

        if (!sharedKeyField.textboxKeyTyped(typedCharacter, keyCode)) {
            super.keyTyped(typedCharacter, keyCode);
        }
    }

    @Override
    protected void mouseClicked(
        int mouseX,
        int mouseY,
        int mouseButton
    ) throws IOException {

        super.mouseClicked(mouseX, mouseY, mouseButton);
        sharedKeyField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(
            fontRendererObj,
            "Unkrypt",
            width / 2,
            contentTop,
            0xFFFFFF
        );

        drawString(
            fontRendererObj,
            "Shared key",
            contentLeft,
            contentTop + 50,
            0xA0A0A0
        );

        sharedKeyField.drawTextBox();

        drawCenteredString(
            fontRendererObj,
            "Unicode support has not been checked",
            width / 2,
            contentTop + 138,
            0xA0A0A0
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
