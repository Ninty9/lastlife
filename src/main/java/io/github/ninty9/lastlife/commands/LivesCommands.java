package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLives;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Objects;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.PlayerLivesList.DisplayLivesMessage;
import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;

public class LivesCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

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
                .argument("lives",IntegerArgumentType.integer(0, config.maxlives))
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
    }



    private static int roll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
        if (PlayerLivesList.IsPlayerOnList(player.getUuid())) {
            PlayerLivesList.ChangeLives(player.getUuid(), (int) (Math.random() * (config.maxlives - config.minlives) + config.minlives));
        } else {
            PlayerLives playerLife = new PlayerLives(player.getUuid(), (int) (Math.random() * (config.maxlives - config.minlives) + config.minlives));
            PlayerLivesList.AddToList(playerLife);
        }
        player.sendMessage(Text.of("Rolling lives..."), false);
        DisplayLivesMessage(player, false);
        return 1;
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
        for (PlayerLives p: playerLivesList)
            if(Objects.equals(p.uuid, player.getUuid()))
                player.sendMessage(new LiteralText(Integer.toString(p.lives)), false);
        return 1;
    }

    private static int change (CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
        PlayerLivesList.ChangeLives(player.getUuid(), context.getArgument("lives", int.class));
        DisplayLivesMessage(player, false);
        return 1;

    }

    private static int update(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerLivesList.ReadToLivesList();
        return 1;
    }

    private static int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerLivesList.ReRollAll();
        return 1;
    }

}
