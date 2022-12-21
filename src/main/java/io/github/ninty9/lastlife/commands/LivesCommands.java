package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Config;
import io.github.ninty9.lastlife.PlayerLives;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import static io.github.ninty9.lastlife.Config.GetRandomLife;
import static io.github.ninty9.lastlife.PlayerLivesList.*;

public class LivesCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignoredDedicated) {

        LiteralCommandNode<ServerCommandSource> livesNode = CommandManager
                .literal("lives")
                .build();

        LiteralCommandNode<ServerCommandSource> rollNode = CommandManager
                .literal("roll")
                .build();

        ArgumentCommandNode<ServerCommandSource, EntitySelector> playerRollNode = CommandManager
                .argument("player", EntityArgumentType.player())
                .executes(LivesCommands::roll)
                .build();

        LiteralCommandNode<ServerCommandSource> getNode = CommandManager
                .literal("get")
                .build();

        ArgumentCommandNode<ServerCommandSource, EntitySelector> playerGetNode = CommandManager
                .argument("player", EntityArgumentType.player())
                .executes(LivesCommands::get)
                .build();

        LiteralCommandNode<ServerCommandSource> changeNode = CommandManager
                .literal("change")
                .build();

        ArgumentCommandNode<ServerCommandSource, EntitySelector> playerArgNode = CommandManager
                .argument("player", EntityArgumentType.player())
                .build();

        ArgumentCommandNode<ServerCommandSource, Integer> livesIntArgNode = CommandManager
                .argument("lives",IntegerArgumentType.integer(0, Config.getMaxLives()))
                .executes(LivesCommands::change)
                .build();

        LiteralCommandNode<ServerCommandSource> updateNode = CommandManager
                .literal("update")
                .executes(LivesCommands::update)
                .build();

        LiteralCommandNode<ServerCommandSource> resetNode = CommandManager
                .literal("reset")
                .executes(LivesCommands::reset)
                .build();

        LiteralCommandNode<ServerCommandSource> clearNode = CommandManager
                .literal("clear")
                .executes(LivesCommands::clear)
                .build();

        dispatcher.getRoot().addChild(livesNode);
        livesNode.addChild(rollNode);
            rollNode.addChild(playerRollNode);
        livesNode.addChild(getNode);
            getNode.addChild(playerGetNode);
        livesNode.addChild(changeNode);
            changeNode.addChild(playerArgNode);
                playerArgNode.addChild(livesIntArgNode);
        livesNode.addChild(updateNode);
        livesNode.addChild(resetNode);
        livesNode.addChild(clearNode);
    }



    private static int roll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //is player isn't on the list, add them with a random lives value, if they are, change their lives to a random lives value
        ServerPlayerEntity sender = context.getSource().getPlayer();
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());

        if (PlayerLivesList.IsPlayerOnList(player.getUuid())) {
            sender.sendMessage(new LiteralText("This player already has lives assigned."), false);
            sender.sendMessage(new LiteralText("Are you sure you want to re-roll their lives?"), false);
            sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
            CommandConfirm.addConfirm(new Confirmation(sender, "livesRoll", player));
        } else {
            PlayerLives playerLife = new PlayerLives(player.getUuid(), GetRandomLife());
            PlayerLivesList.AddToList(playerLife);
            player.sendMessage(new LiteralText("Rolled " + player.getEntityName() + "'s lives."), false);
            player.sendMessage(Text.of("Rolling lives..."), false);
            DisplayLivesMessage(player, false);
            return 1;
        }
        return 0;
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //get a player's lives and display it to the sender
        ServerPlayerEntity sender = context.getSource().getPlayer();

        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
        if(IsPlayerOnList(player)){
            int lives = GetLives(player);
            if(lives == 1)
                sender.sendMessage(new LiteralText(player.getEntityName() + " has 1 life."), false);
            else
                sender.sendMessage(new LiteralText(player.getEntityName() + " has " + lives + " lives."), false);
        } else {
            sender.sendMessage(new LiteralText(player.getEntityName() + " doesn't have any lives."),false);
        }
        return 1;
    }

    private static int change (CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //sets the given players lives to the given amount
        ServerPlayerEntity sender = context.getSource().getPlayer();
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());

        PlayerLivesList.ChangeLives(player.getUuid(), context.getArgument("lives", int.class));
        sender.sendMessage(new LiteralText("Set " + player.getEntityName() + "'s lives to " + context.getArgument("lives", int.class)), false);
        DisplayLivesMessage(player, false);
        return 1;

    }

    private static int update(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //reads from the player lives file and updates the internal list
        //WARNING: this clears the lives file

        ServerPlayerEntity sender = context.getSource().getPlayer();

        sender.sendMessage(new LiteralText("Are you sure?"), false);
        sender.sendMessage(new LiteralText("This will erase all the lives data and replace it with what's in the players.json file."), false);
        sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
        CommandConfirm.addConfirm(new Confirmation(sender, "livesUpdate"));
        return 1;
    }

    private static int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //clears the lives file and re-rolls for everyone online
        //WARNING: this clears the lives file

        ServerPlayerEntity sender = context.getSource().getPlayer();

        sender.sendMessage(new LiteralText("Are you sure?"), false);
        sender.sendMessage(new LiteralText("This will erase all the lives data and re-roll for every online player."), false);
        sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
        CommandConfirm.addConfirm(new Confirmation(sender, "livesReset"));
        return 1;
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //clears the lives file
        //WARNING: this clears the lives file

        ServerPlayerEntity sender = context.getSource().getPlayer();

        sender.sendMessage(new LiteralText("Are you sure?"), false);
        sender.sendMessage(new LiteralText("This will erase all the lives data."), false);
        sender.sendMessage(new LiteralText("Type \"/confirm\" to confirm."), false);
        CommandConfirm.addConfirm(new Confirmation(sender, "livesClear"));
        return 1;
    }

}
