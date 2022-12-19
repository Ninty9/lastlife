package io.github.ninty9.lastlife.commands;

import net.minecraft.server.network.ServerPlayerEntity;

public class Confirmation {


    public ServerPlayerEntity sender;
    public String command;
    public ServerPlayerEntity target;


    public Confirmation(ServerPlayerEntity Sender, String Command) {
        this.sender = Sender;
        this.command = Command;
        this.target = null;
    }

    public Confirmation(ServerPlayerEntity Sender, String Command, ServerPlayerEntity Target) {
        this.sender = Sender;
        this.command = Command;
        this.target = Target;
    }
}
