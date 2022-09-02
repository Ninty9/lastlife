package io.github.ninty9.lastlife.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class RegisterCommands {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(SessionCommands::register);
        CommandRegistrationCallback.EVENT.register(LivesCommands::register);
        CommandRegistrationCallback.EVENT.register(ConfigCommands::register);
    }
}
