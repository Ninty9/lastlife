package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Config;
import io.github.ninty9.lastlife.PlayerLives;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;

public class UpdateConfig{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("UpdateConfig").executes(UpdateConfig::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Config.ReadToConfig();
        return 0;
    }
}
