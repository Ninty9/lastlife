package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.Initializer.*;

public class PlayerLivesList {
    public static List<PlayerLives> playerLivesList = new ArrayList<>();

    public static Team life0;
    public static Team life1;
    public static Team life2;
    public static Team life3;
    public static Team life4;
    public static Team life5;
    public static Team life6;
    public static Team life7;
    public static Team life8;
    public static Team life9;

    public static void SetTeamColors()
    {
        life0 = serverObject.getScoreboard().addTeam("0");
        life1 = serverObject.getScoreboard().addTeam("1");
        life2 = serverObject.getScoreboard().addTeam("2");
        life3 = serverObject.getScoreboard().addTeam("3");
        life4 = serverObject.getScoreboard().addTeam("4");
        life5 = serverObject.getScoreboard().addTeam("5");
        life6 = serverObject.getScoreboard().addTeam("6");
        life7 = serverObject.getScoreboard().addTeam("7");
        life8 = serverObject.getScoreboard().addTeam("8");
        life9 = serverObject.getScoreboard().addTeam("9");

        life0.setColor(Formatting.byCode('7'));
        life1.setColor(Formatting.byCode('4'));
        life2.setColor(Formatting.byCode('e'));
        life3.setColor(Formatting.byCode('a'));
        life4.setColor(Formatting.byCode('2'));
        life5.setColor(Formatting.byCode('2'));
        life6.setColor(Formatting.byCode('b'));
        life7.setColor(Formatting.byCode('b'));
        life8.setColor(Formatting.byCode('1'));
        life9.setColor(Formatting.byCode('1'));


    }

    public static Team GetTeam(int in)
    {
        return switch (in) {
            case 0 -> life0;
            case 1 -> life1;
            case 2 -> life2;
            case 3 -> life3;
            case 4 -> life4;
            case 5 -> life5;
            case 6 -> life6;
            case 7 -> life7;
            case 8 -> life8;
            case 9 -> life9;
            default -> null;
        };
    }


    private static void UpdateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(Initializer.livesPath);
            gson.toJson(playerLivesList, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        for (PlayerLives p: playerLivesList) {
            Initializer.LOGGER.info(p.uuid.toString() + p.lives);
        }
        Initializer.LOGGER.info(playerLivesList.toString());
    }

    public static void AddToList(PlayerLives player)
    {
        try
        {
            boolean match = false;
            Initializer.LOGGER.info("adeding " + playerLivesList.toString());
            LOGGER.info("e");
            if (playerLivesList.isEmpty()) {
                playerLivesList.add(player);
            } else {
                for (PlayerLives p : playerLivesList) {
                    if (Objects.equals(p.uuid, player.uuid)) {
                        playerLivesList.set(playerLivesList.indexOf(p), player);
                        match = true;
                    }
                }
                if (!match) {
                    playerLivesList.add(player);
                }
            }
            if(serverObject != null){
                UpdatePlayer(player.uuid, false);
            }
            UpdateFile();
        }
        catch (ConcurrentModificationException e) {
            LOGGER.info(e.getMessage());
            LOGGER.info(e.toString());
            LOGGER.error(String.valueOf(e));
            e.printStackTrace();
        throw e;
        }
    }

    public static void ReRollAll()
    {
        playerLivesList.clear();
        Collection<ServerPlayerEntity> players = PlayerLookup.all(Initializer.serverObject);
        for (ServerPlayerEntity p : players) {
            AddToList(new PlayerLives(p.getUuid(), (int) (Math.random() * (config.maxlives - config.minlives) + config.minlives)));
            DisplayLivesMessage(p, false);
        }
    }

    public static void ReadToLivesList()
    {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(livesPath);
            PlayerLives[] tempList = gson.fromJson(reader, PlayerLives[].class);
            reader.close();
            playerLivesList.clear();
            for (PlayerLives p: tempList)
                AddToList(p);
            Initializer.LOGGER.info(playerLivesList.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ChangeLives(UUID uuid, int lives) {
        try {
            for (PlayerLives p : playerLivesList) {
                if (Objects.equals(p.uuid, uuid)) {
                    playerLivesList.get(playerLivesList.indexOf(p)).lives = lives;
                    UpdateFile();
                    UpdatePlayer(uuid, false);
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.error(ex.toString());
            throw ex;
        }
    }

    public static void RelativeChangeLives(UUID uuid, int lives) {
        for(PlayerLives p : playerLivesList)
        {
            if(Objects.equals(p.uuid, uuid))
            {
                if (p.lives > 0) {
                    playerLivesList.get(playerLivesList.indexOf(p)).lives += lives;
                    UpdateFile();
                    UpdatePlayer(uuid, false);
                }
            }
        }
    }

    public static void RollPlayer(ServerPlayerEntity player)
    {
        PlayerLives playerLives = new PlayerLives(player.getUuid(), Config.GetRandomLife());
        PlayerLivesList.AddToList(playerLives);
        UpdatePlayer(player, false);
    }

    public static void RollPlayer(UUID uuid)
    {
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        if (player != null){
            RollPlayer(player);
        }
    }

    public static int GetLives(ServerPlayerEntity player) {
        for (PlayerLives p: playerLivesList)
            if (Objects.equals(p.uuid, player.getUuid()))
                return p.lives;
        return 0;
    }

    public static int GetLives(UUID uuid) {
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        return GetLives(player);
    }


    public static void UpdatePlayer(ServerPlayerEntity player, boolean death){
        if(GetLives(player) == 0)
            player.changeGameMode(GameMode.SPECTATOR);
        else
            player.changeGameMode(GameMode.SURVIVAL);

        serverObject.getScoreboard().clearPlayerTeam(player.getEntityName());
        serverObject.getScoreboard().addPlayerToTeam(player.getEntityName(), GetTeam(GetLives(player)));
    }

    public static void UpdatePlayer(UUID uuid, boolean death)
    {
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        UpdatePlayer(player, death);
    }

    public static void DisplayLivesMessage(ServerPlayerEntity player, boolean death) {
        int playerLives = GetLives(player);
        Function<Text, Packet<?>> titleConstructor = TitleS2CPacket::new;

        LiteralText text = new LiteralText(String.valueOf(playerLives));
        text.setStyle(text.getStyle().withColor(GetTeam(playerLives).getColor()));
        try {
            player.networkHandler.sendPacket(titleConstructor.apply(Texts.parse(null, text, null, 0)) );
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        if(death){
            String subtitle;
            TextColor subtitleColor;
            switch (playerLives) {
                case 0 -> {
                    subtitle = "You are dead, for good this time.";
                    subtitleColor = TextColor.parse("gray");
                }
                case 1 -> {
                    subtitle = "This is your last life. Go have some fun.";
                    subtitleColor = TextColor.parse("dark_red");
                }
                case 2 -> {
                    subtitle = "You died. Almost there.";
                    subtitleColor = TextColor.parse("yellow");
                }
                default -> {
                    subtitle = "You died.";
                    subtitleColor = TextColor.parse("white");
                }
            }

            LiteralText subText = new LiteralText(subtitle);
            subText.setStyle(subText.getStyle().withColor(subtitleColor));
            Function<Text, Packet<?>> subtitleConstructor = SubtitleS2CPacket::new;
            try {
                player.networkHandler.sendPacket(subtitleConstructor.apply(Texts.parse(null, subText, null, 0)) );
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean IsPlayerOnList(ServerPlayerEntity player)
    {
        for (PlayerLives p : playerLivesList)
        {
            if(Objects.equals(p.uuid, player.getUuid()))
                return true;
        }
        return false;
    }

    public static boolean IsPlayerOnList(UUID uuid)
    {
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        return IsPlayerOnList(player);
    }
}
