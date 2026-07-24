package com.cewko.unkrypt;

import com.cewko.unkrypt.client.ChatEventHandler;
import com.cewko.unkrypt.client.UnkryptCommand;
import com.cewko.unkrypt.state.UnkryptSession;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = UnkryptMod.MOD_ID,
    name = UnkryptMod.NAME,
    version = UnkryptMod.VERSION,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public final class UnkryptMod {
    public static final String MOD_ID = "unkrypt";
    public static final String NAME = "unkrypt";
    public static final String VERSION = "1.0.0";

    private final UnkryptSession session = new UnkryptSession();
    private final ChatEventHandler eventHandler = new ChatEventHandler(session);

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(eventHandler);

        ClientCommandHandler.instance.registerCommand(
            new UnkryptCommand(eventHandler)
        );
    }
}