package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.*;

import static io.github.ninty9.lastlife.Config.UpdateConfigFile;
import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.Initializer.configPath;
import static io.github.ninty9.lastlife.Initializer.sessionPath;

public class Sessions {
    //sessions keep track of all the players
    //if a player on the list hasn't connected by the time the session ends, remove a life.
    public static List<UUID> playerJoinList = new ArrayList<>();

    public static void addToJoinList(UUID player)
    {
        Initializer.LOGGER.info(player.toString());
        if(config.sessionOn) {
            boolean match = false;
            if (playerJoinList.isEmpty()) {
                playerJoinList.add(player);
            } else {
                for (UUID u : playerJoinList) {
                    if (Objects.equals(u, player)) {
                        playerJoinList.set(playerJoinList.indexOf(u), player);
                        match = true;
                    }
                }
                if (!match)
                    playerJoinList.add(player);
            }
        }
        updateFile();
    }

    public static void clearSession() {
        playerJoinList.clear();
        updateFile();
        config.boogeyman = null;
        UpdateConfigFile();
    }

    public static void endSession() {
        for (PlayerLives p: PlayerLivesList.playerLivesList)
        {
            boolean match = false;
            for (UUID u : playerJoinList)
            {
                if (Objects.equals(u, p.uuid))
                {
                    match = true;
                }
            }
            if(!match)
            {
                PlayerLivesList.RelativeChangeLives(p.uuid, -1);
            }
        }
        playerJoinList.clear();
        updateFile();
        if (config.boogeyman != null)
        {
            PlayerLivesList.RelativeChangeLives(config.boogeyman, -1);
            config.boogeyman = null;
        }
    }

    private static void updateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(sessionPath);
            gson.toJson(playerJoinList, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void ReadToSession()
    {
        try {

            try {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(sessionPath);
                config = gson.fromJson(reader, Config.class);
                reader.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e); }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
