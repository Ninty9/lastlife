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
import net.minecraft.text.TextColor;

import java.util.*;

import static io.github.ninty9.lastlife.Config.UpdateConfigFile;
import static io.github.ninty9.lastlife.Config.isSessionOn;
import static io.github.ninty9.lastlife.Initializer.serverObject;

public class SessionCommands {

    static ArgumentCommandNode<ServerCommandSource, EntitySelector> playerBoogeymanNode;
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignoredDedicated) {

        LiteralCommandNode<ServerCommandSource> sessionNode = CommandManager
                .literal("session").requires(source -> source.hasPermissionLevel(4))
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

        LiteralCommandNode<ServerCommandSource> getNode = CommandManager
                .literal("get")
                .executes(SessionCommands::get)
                .build();

        //usage: /session [start|stop]
        dispatcher.getRoot().addChild(sessionNode);
        sessionNode.addChild(startNode);
        sessionNode.addChild(stopNode);
        sessionNode.addChild(clearNode);
        sessionNode.addChild(boogeymanNode);
            boogeymanNode.addChild(playerBoogeymanNode);
        sessionNode.addChild(getNode);
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
                if(Config.getBoogeymanName() != null)
                    sender.sendMessage(new LiteralText("The boogeyman was " + Config.getBoogeymanName()), false);
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

        if(Config.getBoogeyman() != null) {
            sender.sendMessage(new LiteralText("Session has been cleared, the boogeyman was " + Config.getBoogeymanName()), false);
            ServerPlayerEntity boogey = Config.getBoogeymanPlayer();
            if (boogey != null)
                Config.sendTitle(boogey, "You are no longer the boogeyman!", "The session was cleared, your lives have not gone down.", TextColor.parse("green"), TextColor.parse("green"));
        }
        else
            sender.sendMessage(new LiteralText("Session has been cleared, there was no boogeyman."), false);

        Sessions.clearSession();
        return 1;
    }

    public static int setBoogeyman(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //sets a random boogeyman, with option to add an admin to not get picked

        ServerPlayerEntity sender = context.getSource().getPlayer();

        if (context.getNodes().size() == 3) {
            ServerPlayerEntity boogey = context.getArgument("boogey", EntitySelector.class).getPlayer(context.getSource());
            if (Config.getBoogeyman() != null) {
                return boogeyExistsConfirm(sender, boogey, true);
            }
            var boogeyResult = Config.setBoogeyman(boogey);
            if (boogeyResult) {
                sender.sendMessage(new LiteralText(boogey.getEntityName() + " has been set as boogeyman."), false);
                Config.sendTitle(boogey, "You are the boogeyman!", "Kill someone before the end of the session or lose a life.", TextColor.parse("dark_red"), TextColor.parse("red"));
                return 1;
            } else {
                sender.sendMessage(new LiteralText(boogey.getEntityName() + " only has one life left and probably shouldn't be a boogeyman, if you do want to make them a boogeyman, type \"/confirm\""), false);
                CommandConfirm.addConfirm(new Confirmation(sender, "sessionBoogeyKnown", boogey));
                return 0;
            }
        }

        try {
            List<ServerPlayerEntity> playerList = new ArrayList<>(context.getSource().getServer().getPlayerManager().getPlayerList());
            List<PlayerLives> redList = new ArrayList<>(PlayerLivesList.playerLivesList);
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
                    var boogeyResult = Config.setBoogeyman(boogey);

                    if (boogeyResult){
                        sender.sendMessage(new LiteralText("A player has been set as boogeyman."), false);
                        Config.sendTitle(boogey, "You are the boogeyman!", "Kill someone before the end of the session or lose a life.", TextColor.parse("dark_red"), TextColor.parse("red"));
                        return 1;
                    } else {
                        sender.sendMessage(new LiteralText("Something went wrong, nobody has been set as a boogeyman."), false);
                    }
                } else {
                    return boogeyExistsConfirm(sender, boogey, false);
                }
            }
            sender.sendMessage(new LiteralText("There are no valid players to assign boogeyman to."), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int boogeyExistsConfirm(ServerPlayerEntity sender, ServerPlayerEntity boogey, boolean isSet) {
        sender.sendMessage(new LiteralText("Are you sure?"), false);
        sender.sendMessage(new LiteralText("There is already a boogeyman, this will overwrite that."), false);
        sender.sendMessage(new LiteralText("The current boogeyman is: " + Config.getBoogeymanName() + "."), false);
        sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
        if(isSet)
            CommandConfirm.addConfirm(new Confirmation(sender, "sessionBoogeyKnown", boogey));
        else
            CommandConfirm.addConfirm(new Confirmation(sender, "sessionBoogey", boogey));
        return 0;
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = context.getSource().getPlayer();
        int re;
        if(isSessionOn()) {
            sender.sendMessage(new LiteralText("There is currently a session."), false);
            re = 1;
        }
        else {
            sender.sendMessage(new LiteralText("There isn't a session right now."), false);
            re = 0;
        }

        if(Config.getBoogeymanName() != null)
            sender.sendMessage(new LiteralText("The current boogeyman is: " + Config.getBoogeymanName() + "."), false);
        else
            sender.sendMessage(new LiteralText("There is no boogeyman."), false);
        return re;
    }
}
