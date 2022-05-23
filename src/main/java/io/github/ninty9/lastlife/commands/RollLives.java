package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Initializer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RollLives{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("RollLives").executes(RollLives::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Initializer.LOGGER.info("aeiou");
        context.getSource().getPlayer().damage(DamageSource.CRAMMING, 100);
        return 0;
    }
}
