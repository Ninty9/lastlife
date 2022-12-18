package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.*;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.logging.Logger;

import static io.github.ninty9.lastlife.Config.UpdateConfigFile;
import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.Initializer.LOGGER;

public class SessionCommands {

    static ArgumentCommandNode<ServerCommandSource, EntitySelector> playerBoogeymanNode;
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

        LiteralCommandNode<ServerCommandSource> boogeymanNode = CommandManager
                .literal("boogeyman")
                .executes(SessionCommands::setBoogeyman)
                .build();

        playerBoogeymanNode = CommandManager
                .argument("admin", EntityArgumentType.player())
                .executes(SessionCommands::setBoogeyman)
                .build();

        //usage: /session [start|stop]
        dispatcher.getRoot().addChild(sessionNode);
        sessionNode.addChild(startNode);
        sessionNode.addChild(stopNode);
        sessionNode.addChild(clearNode);
        sessionNode.addChild(boogeymanNode);
            boogeymanNode.addChild(playerBoogeymanNode);
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

    private static int clear(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        Sessions.clearSession();
        return 1;
    }

    public static int setBoogeyman(CommandContext<ServerCommandSource> context){
        try {
            List<ServerPlayerEntity> playerList = new ArrayList<>(context.getSource().getServer().getPlayerManager().getPlayerList());
            LOGGER.info(playerList.toString());
            if (context.getNodes().size() == 3) {
                ServerPlayerEntity admin = context.getArgument("admin", EntitySelector.class).getPlayer(context.getSource());
                playerList.remove(admin);
                LOGGER.info(playerList.toString());
                if (playerList.size() < 1) {
                    //return no boogeyman
                    return 0;
                }
            }

            var redList = PlayerLivesList.playerLivesList;
            redList.removeIf(player -> player.lives > 1);
            LOGGER.info(redList.toString());
            for (var player : playerList) {
                for (var redPlayer : redList) {
                    if (player.getUuid().equals(redPlayer.uuid)) {
                        playerList.remove(player);
                        redList.remove(redPlayer);
                    }
                }
            }

            LOGGER.info(playerList.toString());

            if(!playerList.isEmpty()) {
                ServerPlayerEntity boogey;
                if (playerList.size() > 1) {
                    boogey = playerList.get(new Random().nextInt(playerList.size()));
                } else {
                    boogey = playerList.get(0);
                }
                if (Objects.isNull(boogey)) {
                    throw new RuntimeException("fucked up");
                }
                config.boogeyman = boogey.getUuid();
                LOGGER.info(boogey.getEntityName());
                UpdateConfigFile();
                return 1;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
