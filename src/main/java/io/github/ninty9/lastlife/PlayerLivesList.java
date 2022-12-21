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

import static io.github.ninty9.lastlife.Initializer.*;

public class PlayerLivesList {
    public static List<PlayerLives> playerLivesList = new ArrayList<>();
    private static final Team[] teams = new Team[10];

    public static void SetTeamColors() {
        for(int i = 0; i < 10; i++)
        {
            Team team = serverObject.getScoreboard().getTeam(Integer.toString(i));

            if(team != null) {
                teams[i] = team;
            } else {
                LOGGER.info("Adding team " + i + ".");
                teams[i] = serverObject.getScoreboard().addTeam(Integer.toString(i));
            }
        }

        teams[0].setColor(Formatting.byCode('7'));
        teams[1].setColor(Formatting.byCode('4'));
        teams[2].setColor(Formatting.byCode('e'));
        teams[3].setColor(Formatting.byCode('a'));
        teams[4].setColor(Formatting.byCode('2'));
        teams[5].setColor(Formatting.byCode('2'));
        teams[6].setColor(Formatting.byCode('b'));
        teams[7].setColor(Formatting.byCode('b'));
        teams[8].setColor(Formatting.byCode('1'));
        teams[9].setColor(Formatting.byCode('1'));


    }

    public static Team GetTeam(int in) {
        return switch (in) {
            case 0 -> teams[0];
            case 1 -> teams[1];
            case 2 -> teams[2];
            case 3 -> teams[3];
            case 4 -> teams[4];
            case 5 -> teams[5];
            case 6 -> teams[6];
            case 7 -> teams[7];
            case 8 -> teams[8];
            case 9 -> teams[9];
            default -> null;
        };
    }


    private static void UpdateFile() {
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
    }

    public static void AddToList(PlayerLives player) {
        try
        {
            boolean match = false;
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
                UpdatePlayer(player.uuid);
            }
            UpdateFile();
        }
        catch (ConcurrentModificationException e) {
            e.printStackTrace();
        throw e;
        }
    }

    public static void ReRollAll() {
        playerLivesList.clear();
        Collection<ServerPlayerEntity> players = PlayerLookup.all(Initializer.serverObject);
        for (ServerPlayerEntity p : players) {
            if(!Config.IsExcluded(p)){
                AddToList(new PlayerLives(p.getUuid(), Config.GetRandomLife()));
                p.sendMessage(new LiteralText("Rolling lives..."), false);
                DisplayLivesMessage(p, false);
            }
        }
    }

    public static void ReadToLivesList() {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(livesPath);
            PlayerLives[] tempList = gson.fromJson(reader, PlayerLives[].class);
            reader.close();
            playerLivesList.clear();
            for (PlayerLives p: tempList) {
                AddToList(p);
                UpdatePlayer(p.uuid);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ChangeLives(UUID uuid, int lives) {
        for (PlayerLives p : playerLivesList) {
            if (Objects.equals(p.uuid, uuid)) {
                playerLivesList.get(playerLivesList.indexOf(p)).lives = lives;
                UpdateFile();
                UpdatePlayer(uuid);
            }
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
                    UpdatePlayer(uuid);
                }
            }
        }
    }

    public static void RollPlayer(ServerPlayerEntity player) {
        PlayerLives playerLives = new PlayerLives(player.getUuid(), Config.GetRandomLife());
        PlayerLivesList.AddToList(playerLives);
        UpdatePlayer(player);
    }

    public static void RollPlayer(UUID uuid) {
        PlayerLives playerLives = new PlayerLives(uuid, Config.GetRandomLife());
        PlayerLivesList.AddToList(playerLives);
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        if (player != null){
            UpdatePlayer(player);
        }
    }

    public static int GetLives(ServerPlayerEntity player) {
        for (PlayerLives p: playerLivesList)
            if (Objects.equals(p.uuid, player.getUuid()))
                return p.lives;
        return 0;
    }

    public static int GetLives(UUID uuid) {
        for (PlayerLives p: playerLivesList)
            if (Objects.equals(p.uuid, uuid))
                return p.lives;
        return 0;
    }


    public static void UpdatePlayer(ServerPlayerEntity player){
        if(Config.IsExcluded(player)){
            if (GetLives(player) == 0)
                player.changeGameMode(GameMode.SPECTATOR);
            else
                player.changeGameMode(GameMode.SURVIVAL);
        }

        serverObject.getScoreboard().clearPlayerTeam(player.getEntityName());
        serverObject.getScoreboard().addPlayerToTeam(player.getEntityName(), GetTeam(GetLives(player)));
    }

    public static void UpdatePlayer(UUID uuid)
    {
        if(serverObject != null) {
            ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
            if(player != null)
                UpdatePlayer(player);
        }
    }

    public static void DisplayLivesMessage(ServerPlayerEntity player, boolean death) {
        int playerLives = GetLives(player);
        Function<Text, Packet<?>> titleConstructor = TitleS2CPacket::new;
        Function<Text, Packet<?>> subtitleConstructor = SubtitleS2CPacket::new;

        LiteralText text = new LiteralText(String.valueOf(playerLives));
        text.setStyle(text.getStyle().withColor(GetTeam(playerLives).getColor()));
        try {
            player.networkHandler.sendPacket(titleConstructor.apply(Texts.parse(null, text, null, 0)) );
            player.networkHandler.sendPacket(subtitleConstructor.apply(Texts.parse(null, new LiteralText(""), null, 0)) );
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

    public static void ClearList() {
        playerLivesList.clear();
        UpdateFile();
    }

    public static boolean HasDecay(ServerPlayerEntity player) {
        for (PlayerLives p : playerLivesList)
            if (Objects.equals(p.uuid, player.getUuid()))
                return p.hasDecay;
        return false;
    }

    public static void SetDecay(UUID player, boolean decay) {
        for (PlayerLives p : playerLivesList)
            if (Objects.equals(p.uuid, player))
                p.hasDecay = decay;

    }

}
