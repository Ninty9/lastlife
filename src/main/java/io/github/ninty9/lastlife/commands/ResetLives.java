package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLives;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static io.github.ninty9.lastlife.Initializer.livesPath;
import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;

public class ResetLives {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("ResetLives").executes(ResetLives::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerLivesList.ReadToLivesList();
        return 0;
    }
}
