package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLives;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;

public class GetLives{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("GetLives").executes(GetLives::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        for (PlayerLives p: playerLivesList)
        {
            if(p.uuid == player.getUuid())
                player.sendMessage(new LiteralText(Integer.toString(p.lives)), true);
        }
        return 0;
    }
}
