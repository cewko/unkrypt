package com.cewko.unkrypt.client;

import com.cewko.unkrypt.service.UnicodeSupportProbe;
import com.cewko.unkrypt.state.UnkryptSession;
import com.cewko.unkrypt.crypto.SharedKeyCodec;
import com.cewko.unkrypt.service.UnkryptService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ChatEventHandler {
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
