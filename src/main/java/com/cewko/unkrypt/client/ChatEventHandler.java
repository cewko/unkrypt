package com.cewko.unkrypt.client;

import com.cewko.unkrypt.state.UnkryptSession;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChatEventHandler {
    private final UnkryptSession session;
    private boolean screenOpenRequested;

    public ChatEventHandler(UnkryptSession session) {
        this.session = session;
    }

    public void requestScreenOpen() {
        screenOpenRequested = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !screenOpenRequested) {
            return;
        }

        screenOpenRequested = false;

        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.thePlayer != null) {
            minecraft.displayGuiScreen(new UnkryptScreen(session));
        }
    }
}
