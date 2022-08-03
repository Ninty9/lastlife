package io.github.ninty9.lastlife.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class RegisterCommands {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(RollLives::register);
        CommandRegistrationCallback.EVENT.register(GetLives::register);
        CommandRegistrationCallback.EVENT.register(ResetLives::register);
        CommandRegistrationCallback.EVENT.register(UpdateLives::register);
        CommandRegistrationCallback.EVENT.register(ChangeLives::register);

        CommandRegistrationCallback.EVENT.register(UpdateConfig::register);
    }
}
