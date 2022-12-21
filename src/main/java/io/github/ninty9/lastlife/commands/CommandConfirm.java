package io.github.ninty9.lastlife.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.ninty9.lastlife.Config;
import io.github.ninty9.lastlife.Initializer;
import io.github.ninty9.lastlife.PlayerLivesList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;

import static io.github.ninty9.lastlife.Config.GetRandomLife;
import static io.github.ninty9.lastlife.Config.UpdateConfigFile;

public class CommandConfirm {



    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignoredDedicated) {

        LiteralCommandNode<ServerCommandSource> confirmNode = CommandManager
                .literal("confirm")
                .executes(CommandConfirm::confirm)
                .build();

        dispatcher.getRoot().addChild(confirmNode);

    }

    public static List<Confirmation> confirmList = new ArrayList<>();

    public static int confirm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity sender = context.getSource().getPlayer();

        Confirmation confirm = null;

        for (Confirmation c: confirmList) {
            if(c.sender.equals(sender)) {
                confirm = c;
            }
        }
        confirmList.remove(confirm);

        if(confirm == null) {
            sender.sendMessage(new LiteralText("You have nothing to confirm."), false);
            return 0;
        }

        switch (confirm.command) {
            case "livesUpdate" -> {
                PlayerLivesList.ReadToLivesList();
                for(var p: Initializer.serverObject.getPlayerManager().getPlayerList())
                    if(PlayerLivesList.IsPlayerOnList(p))
                        PlayerLivesList.DisplayLivesMessage(p, false);

                sender.sendMessage(new LiteralText("Updated lives."), false);
                return 1;
            }
            case "livesReset" -> {
                PlayerLivesList.ReRollAll();
                sender.sendMessage(new LiteralText("Re-rolled lives."), false);
                return 1;
            }
            case "livesClear" -> {
                PlayerLivesList.ClearList();
                sender.sendMessage(new LiteralText("Cleared lives."), false);
                return 1;
            }
            case "livesRoll" -> {
                assert confirm.target != null: " Null target for command confirmation";
                PlayerLivesList.ChangeLives(confirm.target.getUuid(), GetRandomLife());
                PlayerLivesList.DisplayLivesMessage(confirm.target, false);
                return 1;
            }
            case "sessionBoogey" -> {
                if (confirm.target != null){
                    Config.setBoogeyman(confirm.target);
                    sender.sendMessage(new LiteralText("A player has been set as boogeyman."), false);
                    Config.sendTitle(confirm.target, "You are the boogeyman!", "Kill someone before the end of the session or lose a life.", TextColor.parse("dark_red"), TextColor.parse("red"));
                    UpdateConfigFile();
                    return 1;
                }
                Initializer.LOGGER.error("something went wrong with boogey confirm");
                return 0;
            }
            case "sessionBoogeyKnown" -> {
                if (confirm.target != null) {
                    Initializer.LOGGER.info(PlayerLivesList.playerLivesList.toString());
                    Config.setBoogeyman(confirm.target);
                    sender.sendMessage(new LiteralText(confirm.target.getEntityName() + " has been set as boogeyman."), false);
                    Config.sendTitle(confirm.target, "You are the boogeyman!", "Kill someone before the end of the session or lose a life.", TextColor.parse("dark_red"), TextColor.parse("red"));
                    UpdateConfigFile();
                    return 1;
                }
                Initializer.LOGGER.error("something went wrong with boogey confirm");
                return 0;
            }
        }

        return 0;
    }

    public static void addConfirm(Confirmation confirm) {
        for (Confirmation c: confirmList) {
            if(c.sender.equals(confirm.sender))
            {
                c.command = confirm.command;
                c.target = confirm.target;
                return;
            }
        }
        confirmList.add(confirm);
    }

}

