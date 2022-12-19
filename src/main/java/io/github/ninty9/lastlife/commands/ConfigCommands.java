package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Config;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class ConfigCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignoreDedicated) {

        LiteralCommandNode<ServerCommandSource> configNode = CommandManager
                .literal("config")
                .build();

        LiteralCommandNode<ServerCommandSource> updateNode = CommandManager
                .literal("update")
                .executes(ConfigCommands::update)
                .build();
        LiteralCommandNode<ServerCommandSource> excludeNode = CommandManager
                .literal("exclude")
                .build();
        ArgumentCommandNode<ServerCommandSource, EntitySelector> playerExcludeNode = CommandManager
                .argument("player", EntityArgumentType.player())
                .executes(ConfigCommands::exclude)
                .build();
        LiteralCommandNode<ServerCommandSource> includeNode = CommandManager
                .literal("include")
                .build();
        ArgumentCommandNode<ServerCommandSource, EntitySelector> playerIncludeNode = CommandManager
                .argument("player", EntityArgumentType.player())
                .executes(ConfigCommands::include)
                .build();


        dispatcher.getRoot().addChild(configNode);
        configNode.addChild(updateNode);
        configNode.addChild(excludeNode);
            excludeNode.addChild(playerExcludeNode);
        configNode.addChild(includeNode);
            includeNode.addChild(playerIncludeNode);
    }

    public static int update(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Reads from the config file and updates internal config class
        ServerPlayerEntity sender = context.getSource().getPlayer();
        sender.sendMessage(new LiteralText("Config updated from file."), false);
        Config.ReadToConfig();
        return 1;
    }


    private static int exclude(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = context.getSource().getPlayer();
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());

        if(Config.addToExcludes(player.getUuid())) {
            sender.sendMessage(new LiteralText(player.getEntityName() + " has been put on the excludes list."), false);
            player.sendMessage(new LiteralText("You have been put on the excludes list."), false);
            return 1;
        }

        sender.sendMessage(new LiteralText(player.getEntityName() + " was already on the excludes list."), false);
        return 0;
    }

    private static int include(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = context.getSource().getPlayer();
        ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());

        if(Config.removeFromExcludes(player.getUuid())) {
            sender.sendMessage(new LiteralText(player.getEntityName() + " has been removed from the excludes list."), false);
            player.sendMessage(new LiteralText("You have been removed from the excludes list."), false);
            return 1;
        }

        sender.sendMessage(new LiteralText(player.getEntityName() + " wasn't on the excludes list."), false);
        return 0;
    }


}
