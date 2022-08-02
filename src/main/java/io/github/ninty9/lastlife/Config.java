package io.github.ninty9.lastlife;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.UUID;

import static io.github.ninty9.lastlife.Initializer.configPath;

public class Config {
    public static Config config = new Config(2, 6);

    int minlives, maxlives;

    public Config(int MinLives, int MaxLives) {
        this.minlives = MinLives;
        this.maxlives = MaxLives;
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
}
