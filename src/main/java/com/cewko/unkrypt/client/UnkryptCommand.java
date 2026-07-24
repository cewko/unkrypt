package com.cewko.unkrypt.client;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class UnkryptCommand extends CommandBase {
    private final ChatEventHandler eventHandler;

    public UnkryptCommand(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public String getCommandName() {
        return "unkrypt";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/unkrypt";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        eventHandler.requestScreenOpen();
    }
}
