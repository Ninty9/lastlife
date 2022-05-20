package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Components;
import io.github.ninty9.lastlife.Initializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class GetLives{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("GetLives").executes(RollLives::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();

        Initializer.LOGGER.info(Integer.toString(Components.LIVES.get(player).getValue()));
        return 0;
    }
}
