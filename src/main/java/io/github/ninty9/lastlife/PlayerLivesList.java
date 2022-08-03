package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.mixin.container.ServerPlayerEntityAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.*;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.Initializer.livesPath;

public class PlayerLivesList {
    public static List<PlayerLives> playerLivesList = new ArrayList<PlayerLives>();

    public static void UpdateFile()
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
        boolean match = false;
        Initializer.LOGGER.info( "adding " + playerLivesList.toString());
        if(playerLivesList.isEmpty()) {
                playerLivesList.add(player);
        } else {
            for (PlayerLives p : playerLivesList) {
                if (Objects.equals(p.uuid, player.uuid)) {
                    playerLivesList.set(playerLivesList.indexOf(p), player);
                    match = true;
                }

            }
            if(!match)
                playerLivesList.add(player);
        }

        UpdateFile();
    }

    public static void RerollAll()
    {
        try {
            playerLivesList.clear();
            Collection<ServerPlayerEntity> players = PlayerLookup.all(Initializer.serverObject);
            for (ServerPlayerEntity p : players)
                AddToList(new PlayerLives(p.getUuid(), (int) (Math.random() * (config.maxlives - config.minlives) + config.minlives)));
        } catch (Exception ex)
        {
            throw ex;
        }
    }

    public static void ReadToLivesList()
    {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(livesPath);
            List<PlayerLives> tempList = Arrays.asList(gson.fromJson(reader, PlayerLives[].class));
            reader.close();
            playerLivesList.clear();
            for (PlayerLives p: tempList)
                AddToList(p);
            Initializer.LOGGER.info(playerLivesList.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ChangeLives(UUID uuid, int lives)
    {
        for(PlayerLives p : playerLivesList)
        {
            if(Objects.equals(p.uuid, uuid))
            {
                playerLivesList.get(playerLivesList.indexOf(p)).lives = lives;
                UpdateFile();
                UpdateGameMode(uuid);
            }
        }
    }

    public static void DecreaseLivesByOne(UUID uuid) {
        for(PlayerLives p : playerLivesList)
        {
            if(Objects.equals(p.uuid, uuid))
            {
                if (p.lives > 0) {
                    playerLivesList.get(playerLivesList.indexOf(p)).lives = p.lives - 1;
                    UpdateFile();
                    UpdateGameMode(uuid);
                }
            }
        }
    }

    public static int GetLives(UUID uuid) {
        for (PlayerLives p: playerLivesList)
            if (Objects.equals(p.uuid, uuid))
                return p.lives;
        return 0;
    }
    public static int Getlives(ServerPlayerEntity player) {
        for (PlayerLives p: playerLivesList)
            if (Objects.equals(p.uuid, player.getUuid()))
                return p.lives;
        return 0;
    }

    public static void UpdateGameMode(ServerPlayerEntity player)
    {
        if(Getlives(player) == 0)
            player.changeGameMode(GameMode.SPECTATOR);
        else
            player.changeGameMode(GameMode.SURVIVAL);
    }

    public static void UpdateGameMode(UUID uuid)
    {
        ServerPlayerEntity player = Initializer.serverObject.getPlayerManager().getPlayer(uuid);
        if(player != null) {
            if (Getlives(player) == 0)
                player.changeGameMode(GameMode.SPECTATOR);
            else
                player.changeGameMode(GameMode.SURVIVAL);
        }
    }
}
