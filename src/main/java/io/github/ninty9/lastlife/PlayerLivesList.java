package io.github.ninty9.lastlife;

import com.google.gson.Gson;

import java.io.Writer;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PlayerLivesList {
    public static List<PlayerLives> playerLivesList = new ArrayList<PlayerLives>();

    public static void AddToList(PlayerLives player)
    {
        boolean match = false;
        if(playerLivesList.isEmpty()) {
            playerLivesList.add(player);
        } else {
            for (PlayerLives p : playerLivesList) {
                if (p.uuid == player.uuid) {
                    playerLivesList.set(playerLivesList.indexOf(p), player);
                    match = true;
                }
            }
        }
        if(!match)
            playerLivesList.add(player);
        UpdateFile();
        Initializer.LOGGER.info(playerLivesList.toString());
    }

    public static void UpdateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(Initializer.configPath);
            gson.toJson(playerLivesList, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
