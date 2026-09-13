package com.cewko.unkrypt.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class UnkryptKeyBindings {

    private static final String CATEGORY = "key.categories.unkrypt";
    private static final int DEFAULT_OPEN_MENU_KEY = Keyboard.KEY_U;
    private static final int DEFAULT_TOGGLE_ENCRYPTION_KEY = Keyboard.KEY_O;
    private static final int DEFAULT_TOGGLE_DECRYPTION_KEY = Keyboard.KEY_NONE;

    private final PressTracker encryptionPress = new PressTracker();
    private final PressTracker decryptionPress = new PressTracker();

    private static final class PressTracker {

        private boolean wasDown;

        public boolean consume(KeyBinding binding) {
            boolean pressed = false;

            while (binding.isPressed()) {
                pressed = true;
            }

            boolean firstPress = pressed && !wasDown;
            wasDown = binding.isKeyDown();

            return firstPress;
        }
    }

    private final KeyBinding openMenu = new KeyBinding(
        "key.unkrypt.openMenu",
        DEFAULT_OPEN_MENU_KEY,
        CATEGORY
    );

    private final KeyBinding toggleEncryption = new KeyBinding(
        "key.unkrypt.toggleEncryption",
        DEFAULT_TOGGLE_ENCRYPTION_KEY,
        CATEGORY
    );

    private final KeyBinding toggleDecryption = new KeyBinding(
        "key.unkrypt.toggleDecryption",
        DEFAULT_TOGGLE_DECRYPTION_KEY,
        CATEGORY
    );

    public void register() {
        ClientRegistry.registerKeyBinding(openMenu);
        ClientRegistry.registerKeyBinding(toggleEncryption);
        ClientRegistry.registerKeyBinding(toggleDecryption);
    }

    public boolean consumeOpenMenuPress() {
        boolean pressed = false;

        while (openMenu.isPressed()) {
            pressed = true;
        }

        return pressed;
    }

    public boolean consumeEncryptionPress() {
        return encryptionPress.consume(toggleEncryption);
    }

    public boolean consumeDecryptionPress() {
        return decryptionPress.consume(toggleDecryption);
    }
}
