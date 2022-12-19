package io.github.ninty9.lastlife;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.*;

import static io.github.ninty9.lastlife.Config.IsExcluded;
import static io.github.ninty9.lastlife.Config.UpdateConfigFile;
import static io.github.ninty9.lastlife.Initializer.sessionPath;

public class Sessions {
    //sessions keep track of all the players
    //if a player on the list hasn't connected by the time the session ends, remove a life.
    public static List<UUID> playerJoinList = new ArrayList<>();

    public static void addToJoinList(UUID player)
    {
        if(Config.isSessionOn()) {
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
        Config.clearBoogeyman();
        UpdateConfigFile();
    }

    public static void endSession() {
        for (PlayerLives p: PlayerLivesList.playerLivesList)
        {
            boolean match = false;
            for (UUID u : playerJoinList)
            {
                if (Objects.equals(u, p.uuid)) {
                    match = true;
                    break;
                }
            }
            if(!match && !IsExcluded(p.uuid))
            {
                PlayerLivesList.RelativeChangeLives(p.uuid, -1);
                p.hasDecay = true;
            }
        }
        playerJoinList.clear();
        updateFile();
        if (Config.getBoogeyman() != null)
        {
            PlayerLivesList.RelativeChangeLives(Config.getBoogeyman(), -1);
            Config.clearBoogeyman();
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
                UUID[] tempSession = gson.fromJson(reader, UUID[].class);
                playerJoinList.addAll(Arrays.asList(tempSession));
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
