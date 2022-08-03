package io.github.ninty9.lastlife;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.UUID;

import static io.github.ninty9.lastlife.Initializer.configPath;

public class Config {
    public static Config config;

    public int minlives, maxlives;
    public boolean rollOnJoin;

    public Config(int MinLives, int MaxLives, boolean RollOnJoin) {
        this.minlives = MinLives;
        this.maxlives = MaxLives;
        this.rollOnJoin = RollOnJoin;
    }
    public static void UpdateFile()
    {
        try {
            Gson gson = new Gson();
            Writer writer = Files.newBufferedWriter(configPath);
            gson.toJson(config, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public static void ReadToConfig()
    {
        try {

            try {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(configPath);
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

    public static int GetRandomLife()
    {
        return (int) (Math.random() * (config.maxlives - config.minlives) + config.minlives);
    }
}
