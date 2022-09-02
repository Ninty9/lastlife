package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Config;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ConfigCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        LiteralCommandNode<ServerCommandSource> configNode = CommandManager
                .literal("config")
                .build();

        LiteralCommandNode<ServerCommandSource> updateNode = CommandManager
                .literal("update")
                .executes(ConfigCommands::update)
                .build();

        dispatcher.getRoot().addChild(configNode);
        configNode.addChild(updateNode);
    }

    public static int update(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Config.ReadToConfig();
        return 0;
    }
}
