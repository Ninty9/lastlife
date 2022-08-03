package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLives;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.fabricmc.fabric.mixin.container.ServerPlayerEntityAccessor;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import java.util.Objects;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChangeLives{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("ChangeLives")
                .then(argument("player", EntityArgumentType.player())
                        .then(argument("lives",IntegerArgumentType.integer(0, config.maxlives))
                                .executes(ChangeLives::run))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
        PlayerLivesList.ChangeLives(player.getUuid(), context.getArgument("lives", int.class));
        return 0;
    }
}
