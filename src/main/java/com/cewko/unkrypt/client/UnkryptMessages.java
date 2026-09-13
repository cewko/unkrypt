package com.cewko.unkrypt.client;

import com.cewko.unkrypt.UnkryptMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public final class UnkryptMessages {

    private UnkryptMessages() {}

    private static final String PREFIX = "[" + UnkryptMod.NAME + "] ";

    public static void error(String message) {
        print(EnumChatFormatting.RED + PREFIX + message);
    }

    public static void encryptionState(boolean enabled) {
        String state = enabled ? "encrypted" : "unencrypted";
        printState("Your outgoing messages are ", state, enabled);
    }

    public static void decryptionState(boolean enabled) {
        String state = enabled ? "ON" : "OFF";
        printState("Decryption for your incoming messages is ", state, enabled);
    }

    private static void printState(
        String description,
        String state,
        boolean enabled
    ) {
        EnumChatFormatting color = enabled
            ? EnumChatFormatting.GREEN
            : EnumChatFormatting.RED;

        print(
            EnumChatFormatting.GOLD +
                PREFIX +
                EnumChatFormatting.WHITE +
                description +
                color +
                state
        );
    }

    private static void print(String message) {
        Minecraft.getMinecraft()
            .ingameGUI.getChatGUI()
            .printChatMessage(new ChatComponentText(message));
    }
}
