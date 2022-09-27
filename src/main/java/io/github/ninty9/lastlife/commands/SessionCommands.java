package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Config;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.Sessions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static io.github.ninty9.lastlife.Config.config;

public class SessionCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        LiteralCommandNode<ServerCommandSource> sessionNode = CommandManager
                .literal("session")
                .build();

        LiteralCommandNode<ServerCommandSource> startNode = CommandManager
                .literal("start")
                .executes(SessionCommands::start)
                .build();

        LiteralCommandNode<ServerCommandSource> stopNode = CommandManager
                .literal("stop")
                .executes(SessionCommands::stop)
                .build();

        LiteralCommandNode<ServerCommandSource> clearNode = CommandManager
                .literal("clear")
                .executes(SessionCommands::clear)
                .build();

        //usage: /session [start|stop]
        dispatcher.getRoot().addChild(sessionNode);
        sessionNode.addChild(startNode);
        sessionNode.addChild(stopNode);
        sessionNode.addChild(clearNode);
    }



    private static int start(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (!config.sessionOn)
        {
            config.sessionOn = true;
            Config.UpdateConfigFile();
            for (ServerPlayerEntity p : Initializer.serverObject.getPlayerManager().getPlayerList())
            {
                Sessions.addToJoinList(p.getUuid());
            }
            return 1;
        }
        return 0;
    }

    private static int stop(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (config.sessionOn)
        {
            config.sessionOn = false;
            Config.UpdateConfigFile();
            //config stop method in file to handle lives processing
            Sessions.endSession();
            return 1;
        }
        return 0;
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Config.ReadToConfig();
        return 1;
    }

    private static int clear(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        Sessions.clearSession();
        return 1;
    }
}
