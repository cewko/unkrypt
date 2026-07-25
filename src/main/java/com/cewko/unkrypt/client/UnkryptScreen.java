package com.cewko.unkrypt.client;

import com.cewko.unkrypt.UnkryptMod;
import com.cewko.unkrypt.service.UnicodeSupportProbe;
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
    private static final int CONTROLS_HEIGHT = 148;

    private final UnkryptSession session;

    private GuiButton encryptButton;
    private GuiButton decryptButton;
    private GuiTextField sharedKeyField;

    private int contentLeft;
    private int controlsTop;

    private final UnicodeSupportProbe unicodeSupportProbe;
    private GuiButton unicodeCheckButton;

    public UnkryptScreen(
        UnkryptSession session,
        UnicodeSupportProbe unicodeSupportProbe
    ) {
        this.session = session;
        this.unicodeSupportProbe = unicodeSupportProbe;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        contentLeft = (width - CONTENT_WIDTH) / 2;
        controlsTop = (height - CONTROLS_HEIGHT) / 2;

        encryptButton = new GuiButton(
            ENCRYPT_BUTTON_ID,
            contentLeft,
            controlsTop,
            100,
            20,
            ""
        );

        decryptButton = new GuiButton(
            DECRYPT_BUTTON_ID,
            contentLeft + 104,
            controlsTop,
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
            controlsTop + 40,
            CONTENT_WIDTH,
            20
        );

        sharedKeyField.setMaxStringLength(64);
        sharedKeyField.setEnabled(false);

        addDisabledKeyButton(
            NEW_KEY_BUTTON_ID,
            contentLeft,
            controlsTop + 66,
            "New"
        );

        addDisabledKeyButton(
            COPY_BUTTON_ID,
            contentLeft + 52,
            controlsTop + 66,
            "Copy"
        );

        addDisabledKeyButton(
            PASTE_BUTTON_ID,
            contentLeft + 104,
            controlsTop + 66,
            "Paste"
        );

        addDisabledKeyButton(
            SHOW_BUTTON_ID,
            contentLeft + 156,
            controlsTop + 66,
            "Show"
        );

        unicodeCheckButton = new GuiButton(
            UNICODE_CHECK_BUTTON_ID,
            contentLeft,
            controlsTop + 92,
            CONTENT_WIDTH,
            20,
            "Check Unicode support"
        );

        unicodeCheckButton.enabled = !unicodeSupportProbe.isRunning();
        buttonList.add(unicodeCheckButton);

        buttonList.add(new GuiButton(
            DONE_BUTTON_ID,
            contentLeft,
            controlsTop + 128,
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

            case UNICODE_CHECK_BUTTON_ID:
                if (mc.thePlayer != null && !unicodeSupportProbe.isRunning()) {
                    String probeMessage = unicodeSupportProbe.start(System.nanoTime());

                    mc.thePlayer.sendChatMessage(probeMessage);
                    unicodeCheckButton.enabled = false;
                }
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
        unicodeCheckButton.enabled = !unicodeSupportProbe.isRunning();
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
            UnkryptMod.NAME + " v" + UnkryptMod.VERSION,
            width / 2,
            controlsTop - 22,
            0xFFFFFF
        );

        drawString(
            fontRendererObj,
            "Shared key",
            contentLeft,
            controlsTop + 28,
            0xA0A0A0
        );

        sharedKeyField.drawTextBox();

        drawCenteredString(
            fontRendererObj,
            unicodeSupportProbe.getStatusMessage(),
            width / 2,
            controlsTop + 116,
            0xA0A0A0
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
