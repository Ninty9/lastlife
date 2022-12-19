package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.*;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.*;

import static io.github.ninty9.lastlife.Config.UpdateConfigFile;

import static io.github.ninty9.lastlife.Initializer.LOGGER;
import static io.github.ninty9.lastlife.Initializer.serverObject;

public class SessionCommands {

    static ArgumentCommandNode<ServerCommandSource, EntitySelector> playerBoogeymanNode;
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignoredDedicated) {

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
                .argument("boogey", EntityArgumentType.player())
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



    private static int start(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //starts a session and adds all online players to the join list
        ServerPlayerEntity sender = context.getSource().getPlayer();

        if (!Config.isSessionOn())
        {
            Config.setSessionOn(true);
            Config.UpdateConfigFile();
            for (ServerPlayerEntity p : Initializer.serverObject.getPlayerManager().getPlayerList())
            {
                Sessions.addToJoinList(p.getUuid());
            }

            for (ServerPlayerEntity p: Initializer.serverObject.getPlayerManager().getPlayerList()) {
                p.sendMessage(new LiteralText("Session has started!"), false);
            }
            return 1;
        }
        sender.sendMessage(new LiteralText("Session is already active."), false);
        return 0;
    }

    private static int stop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //stops the session and triggers the end session logic
        ServerPlayerEntity sender = context.getSource().getPlayer();

        if (Config.isSessionOn())
        {
            Config.setSessionOn(false);
            Config.UpdateConfigFile();

            //config stop method in file to handle lives processing
            Sessions.endSession();

            for (ServerPlayerEntity p: Initializer.serverObject.getPlayerManager().getPlayerList()) {
                p.sendMessage(new LiteralText("Session has ended!"), false);
                ServerPlayerEntity boogey = Config.getBoogeymanPlayer();
                if(boogey != null)
                    sender.sendMessage(new LiteralText("The boogeyman was " + boogey.getEntityName()), false);
                else if (Config.getBoogeyman() != null)
                    sender.sendMessage(new LiteralText("The boogeyman was offline."), false);
                else
                    sender.sendMessage(new LiteralText("There was no boogeyman."), false);
            }

            return 1;
        }

        sender.sendMessage(new LiteralText("There is no active session."), false);
        return 0;
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //stops the session without triggering the end session logic, this won't remove lives from offline players
        ServerPlayerEntity sender = context.getSource().getPlayer();
        for ( var p: serverObject.getPlayerManager().getPlayerList() )
            if (!p.equals(sender))
                p.sendMessage(new LiteralText("Session has been cleared."), false);

        if(Config.getBoogeyman() != null)
            sender.sendMessage(new LiteralText("Session has been cleared, the boogeyman was " + Config.getBoogeymanName()), false);
        else
            sender.sendMessage(new LiteralText("Session has been cleared, there was no boogeyman."), false);

        Sessions.clearSession();
        Config.clearBoogeyman();
        return 1;
    }

    public static int setBoogeyman(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //sets a random boogeyman, with option to add an admin to not get picked

        ServerPlayerEntity sender = context.getSource().getPlayer();

        if (context.getNodes().size() == 3) {
            ServerPlayerEntity boogey = context.getArgument("boogey", EntitySelector.class).getPlayer(context.getSource());
            if(Config.getBoogeyman() == null){
                Config.setBoogeyman(boogey);
                UpdateConfigFile();
                sender.sendMessage(new LiteralText(boogey.getEntityName() + " has been set as boogeyman."), true);
                //todo: tell the boogeyman that they're boogeyman with title
                return 1;
            } else {
                return boogeyExistsConfirm(sender, boogey);
            }
        }

        try {
            List<ServerPlayerEntity> playerList = new ArrayList<>(context.getSource().getServer().getPlayerManager().getPlayerList());
            var redList = PlayerLivesList.playerLivesList;
            redList.removeIf(player -> player.lives > 1);

            List<UUID> removeList = new ArrayList<>();
            for (PlayerLives p: redList) {
                removeList.add(p.uuid);
            }
            removeList.addAll(Config.getExcludes());
            List<ServerPlayerEntity> boogeysList = new ArrayList<>(playerList);

            for (var player : playerList) {
                for (var r : removeList) {
                    if (player.getUuid().equals(r)) {
                        boogeysList.remove(player);
                    }
                }
            }

            if(!boogeysList.isEmpty()) {

                ServerPlayerEntity boogey;

                if (boogeysList.size() > 1)
                    boogey = boogeysList.get(new Random().nextInt(boogeysList.size()));
                else
                    boogey = boogeysList.get(0);

                if (Objects.isNull(boogey))
                    throw new RuntimeException("fucked up");

                if(Config.getBoogeyman() == null) {
                    Config.setBoogeyman(boogey);
                    //todo: tell target that they're boogey with title
                    sender.sendMessage(new LiteralText(boogey.getEntityName() + " has been set as boogeyman."), true);
                    UpdateConfigFile();
                    return 1;
                } else {
                    return boogeyExistsConfirm(sender, boogey);
                }
            }
            sender.sendMessage(new LiteralText("There are no valid players to assign boogeyman to."), false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private static int boogeyExistsConfirm(ServerPlayerEntity sender, ServerPlayerEntity boogey) {
        sender.sendMessage(new LiteralText("Are you sure?"), false);
        sender.sendMessage(new LiteralText("There is already a boogeyman, this will overwrite that."), false);
        sender.sendMessage(new LiteralText("The current boogeyman is: " + Config.getBoogeymanName() + "."), false);
        sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
        CommandConfirm.addConfirm(new Confirmation(sender, "sessionBoogey", boogey));
        return 0;
    }
}
