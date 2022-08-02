package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.*;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.Initializer.livesPath;

public class PlayerLivesList {
    public static List<PlayerLives> playerLivesList = new ArrayList<PlayerLives>();

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


}
