package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Writer;
import java.nio.file.Files;
import java.util.*;

import static io.github.ninty9.lastlife.Config.config;

public class Sessions {
    //sessions keep track of all the players
    //if a player on the list hasn't connected by the time the session ends, remove a life.
    public static List<UUID> playerJoinList = new ArrayList<>();

    /*
     * todo:
     *  when session ends, subtract one from every player on the lives list but not on the session list
     */

    //currently, shits fuckin no worky motherfucker
    //help

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
                    if (!match)
                        playerJoinList.add(player);
                }
            }
        }
        updateFile();
    }

    public static void clearSession() {
        playerJoinList.clear();
        updateFile();
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
    }

    private static void updateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(Initializer.sessionPath);
            gson.toJson(playerJoinList, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
