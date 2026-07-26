package com.cewko.unkrypt.client;

import com.cewko.unkrypt.service.UnicodeSupportProbe;
import com.cewko.unkrypt.state.UnkryptSession;
import com.cewko.unkrypt.crypto.SharedKeyCodec;
import com.cewko.unkrypt.service.UnkryptService;
import com.cewko.unkrypt.transport.TransportEnvelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ChatEventHandler {
    private static final String DECRYPTED_MESSAGE_INDICATOR = "[u] "; 
    private static final Method SET_CHAT_LINE_METHOD =
        ReflectionHelper.findMethod(
            GuiNewChat.class,
            null,
            new String[] { "setChatLine", "func_146237_a" },
            IChatComponent.class,
            int.class,
            int.class,
            boolean.class
        );

    private final UnkryptSession session;
    private final SharedKeyCodec sharedKeyCodec;
    private final UnicodeSupportProbe unicodeSupportProbe;
    private final UnkryptService unkryptService;
    private boolean screenOpenRequested;

    public ChatEventHandler(
        UnkryptSession session,
        UnicodeSupportProbe unicodeSupportProbe,
        SharedKeyCodec sharedKeyCodec,
        UnkryptService unkryptService
    ) {
        this.session = session;
        this.unicodeSupportProbe = unicodeSupportProbe;
        this.sharedKeyCodec = sharedKeyCodec;
        this.unkryptService = unkryptService;
    }

    public void requestScreenOpen() {
        screenOpenRequested = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        unicodeSupportProbe.updateTimeout(System.nanoTime());

        if (!screenOpenRequested) {
            return;
        }

        screenOpenRequested = false;

        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.thePlayer != null) {
            minecraft.displayGuiScreen(
                new UnkryptScreen(
                    session, 
                    unicodeSupportProbe,
                    sharedKeyCodec
                )
            );
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.type == 2) {
            return;
        }

        String incomingMessage = event.message.getUnformattedText();

        if (unicodeSupportProbe.inspectIncoming(incomingMessage)) {
            event.setCanceled(true);
            return;
        }

        if (!session.isDecryptionEnabled()) {
            return;
        }

        if (!session.hasSharedKey()) {
            return;
        }

        for (int index = 0; index < incomingMessage.length(); index++) {
            String possibleEncryptedMessage = incomingMessage.substring(index);

            if (!TransportEnvelope.startsWithMarker(possibleEncryptedMessage)) {
                continue;
            }

            try {
                String plaintext = unkryptService.decrypt(
                    session.getSharedKey(),
                    possibleEncryptedMessage
                );

                String formattedMessage = event.message.getFormattedText();
                int encryptedPosition = formattedMessage.indexOf(possibleEncryptedMessage);

                if (encryptedPosition < 0) {
                    continue;
                }

                String formattedPrefix = formattedMessage.substring(0, encryptedPosition);

                ChatComponentText displayedMessage = new ChatComponentText(formattedPrefix);
                ChatComponentText indicator = new ChatComponentText(DECRYPTED_MESSAGE_INDICATOR);

                displayedMessage.appendSibling(indicator);
                displayedMessage.appendText(plaintext);

                replaceIncomingMessageWithoutLogging(event, displayedMessage);
                return;
            } catch (IllegalArgumentException exception) {
            } catch (GeneralSecurityException exception) {
            }
        }
    }

    private void replaceIncomingMessageWithoutLogging(
        ClientChatReceivedEvent event,
        IChatComponent message
    ) {
        event.setCanceled(true);

        Minecraft minecraft = Minecraft.getMinecraft();
        GuiNewChat chatGui = minecraft.ingameGUI.getChatGUI();

        try {
            SET_CHAT_LINE_METHOD.invoke(
                chatGui, message, 0, minecraft.ingameGUI.getUpdateCounter(), false
            );
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                "couldn't access minecraft's chat display method", exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                "minecraft couldn't display the decrypted message",
                exception.getCause()
            );
        }
    }

    @SubscribeEvent
    public void onClientConnected(
        FMLNetworkEvent.ClientConnectedToServerEvent event
    ) {
        unicodeSupportProbe.reset();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null || event.gui.getClass() != GuiChat.class) {
            return;
        }

        GuiChat originalChat = (GuiChat) event.gui;
        String defaultText = ObfuscationReflectionHelper.getPrivateValue(
            GuiChat.class,
            originalChat,
            "field_146409_v"
        );

        event.gui = new EncryptingGuiChat(defaultText, session, unkryptService);
    }
}
