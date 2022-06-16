package io.github.ninty9.lastlife.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLives;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class RollLives{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("RollLives").requires(source -> source.hasPermissionLevel(2))
                        .executes(RollLives::run));
    }


    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerLives player = new PlayerLives(context.getSource().getPlayer().getUuid(), 7);
        PlayerLivesList.AddToList(player);
        return 0;
    }
}
