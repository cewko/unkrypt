package com.cewko.unkrypt;

import com.cewko.unkrypt.client.ChatEventHandler;
import com.cewko.unkrypt.client.UnkryptCommand;
import com.cewko.unkrypt.service.UnicodeSupportProbe;
import com.cewko.unkrypt.state.UnkryptSession;
import com.cewko.unkrypt.crypto.SharedKeyCodec;

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
    public static final String NAME = "Unkrypt";
    public static final String VERSION = "1.0.0";

    private final UnkryptSession session = new UnkryptSession();
    private final SharedKeyCodec sharedKeyCodec = new SharedKeyCodec();
    private final UnicodeSupportProbe unicodeSupportProbe = new UnicodeSupportProbe();

    private final ChatEventHandler eventHandler = new ChatEventHandler(
        session, unicodeSupportProbe, sharedKeyCodec
    );

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        session.setSharedKey(sharedKeyCodec.generate());
        MinecraftForge.EVENT_BUS.register(eventHandler);

        ClientCommandHandler.instance.registerCommand(
            new UnkryptCommand(eventHandler)
        );
    }
}