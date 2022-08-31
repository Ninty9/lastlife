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


    public static void AddToJoinList(UUID player)
    {
        if(config.sessionOn) {
            boolean match = false;
            for (UUID u : playerJoinList) {
                if (Objects.equals(u, player)) {
                    playerJoinList.set(playerJoinList.indexOf(u), player);
                    match = true;
                }
                if (!match)
                    playerJoinList.add(player);
            }
        }
        UpdateFile();
    }

    private static void UpdateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(Initializer.livesPath);
            gson.toJson(playerJoinList, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
