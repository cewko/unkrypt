package com.cewko.unkrypt.client;

import com.cewko.unkrypt.service.UnicodeSupportProbe;
import com.cewko.unkrypt.state.UnkryptSession;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChatEventHandler {
    private final UnkryptSession session;
    private final UnicodeSupportProbe unicodeSupportProbe;
    private boolean screenOpenRequested;

    public ChatEventHandler(
        UnkryptSession session,
        UnicodeSupportProbe unicodeSupportProbe
    ) {
        this.session = session;
        this.unicodeSupportProbe = unicodeSupportProbe;
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
                new UnkryptScreen(session, unicodeSupportProbe)
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
}
