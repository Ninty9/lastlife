package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Texts;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static io.github.ninty9.lastlife.Initializer.configPath;
import static io.github.ninty9.lastlife.Initializer.serverObject;

public class Config {

    private static Config config;
    //public static Config getConfig(){ return config; }
    public static void setConfig(Config newConfig) {config = newConfig;}


    private final int minLives, maxLives;
    //public static int getMinLives() { return config.minLives; }
    public static int getMaxLives() { return config.maxLives; }

    private final boolean rollOnJoin;
    public static boolean isRollOnJoin() { return config.rollOnJoin; }

    private boolean sessionOn;
    public static boolean isSessionOn() { return config.sessionOn; }
    public static void setSessionOn(boolean sessionOn) { config.sessionOn = sessionOn; }

    private UUID boogeyman;
    public static UUID getBoogeyman() { return config.boogeyman; }
    public static void clearBoogeyman() {
        config.boogeyman = null;
        config.boogeymanName = null;
        UpdateFile();
    }

    private final List<UUID> excludes;
    public static List<UUID> getExcludes() { return config.excludes; }
    public static boolean addToExcludes(UUID exclude) {
        if(!Config.getExcludes().contains(exclude)) {
            config.excludes.add(exclude);
        return true;
        }
        return false;
    }

    public static boolean removeFromExcludes(UUID exclude) {
        if(Config.getExcludes().contains(exclude)) {
            config.excludes.remove(exclude);
            return true;
        }
        return false;
    }

    private String boogeymanName;
    public static String getBoogeymanName() { return config.boogeymanName; }

    public static boolean setBoogeyman(ServerPlayerEntity boogey) {
        if (PlayerLivesList.GetLives(boogey) > 1) {
            return false;
        }
        config.boogeyman = boogey.getUuid();
        config.boogeymanName = boogey.getEntityName();
        UpdateConfigFile();
        return true;
    }

    public Config(int MinLives, int MaxLives, boolean RollOnJoin, boolean SessionOn, UUID Boogeyman, List<UUID> Excludes, String BoogeymanName) {
        this.minLives = MinLives;
        this.maxLives = MaxLives;
        this.rollOnJoin = RollOnJoin;
        this.sessionOn = SessionOn;
        this.boogeyman = Boogeyman;
        this.excludes = Excludes;
        this.boogeymanName = BoogeymanName;
    }
    private static void UpdateFile()
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
    public static void UpdateConfigFile() { UpdateFile(); }

    public static void ReadToConfig()
    {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(configPath);
            config = gson.fromJson(reader, Config.class);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int GetRandomLife() { return (int) (Math.random() * (config.maxLives - config.minLives) + config.minLives); }

    public static boolean IsExcluded(UUID uuid) { return config.excludes.contains(uuid); }
    public static boolean IsExcluded(ServerPlayerEntity player) { return IsExcluded(player.getUuid()); }

    public static ServerPlayerEntity getBoogeymanPlayer(){
        if(config.boogeyman != null)
            for (var p: serverObject.getPlayerManager().getPlayerList())
                if(p.getUuid().equals(config.boogeyman))
                    return p;
        return null;
    }

    public static void sendTitle(ServerPlayerEntity target, String titleIn, String subTitleIn, TextColor titleColor, TextColor subtitleColor){
        LiteralText title = new LiteralText(titleIn);
        title.setStyle(title.getStyle().withColor(titleColor));
        LiteralText subTitle = new LiteralText(subTitleIn);
        subTitle.setStyle(subTitle.getStyle().withColor(subtitleColor));
        Function<Text, Packet<?>> subtitleConstructor = SubtitleS2CPacket::new;
        try {
            Function<Text, Packet<?>> titleConstructor = TitleS2CPacket::new;
            target.networkHandler.sendPacket(titleConstructor.apply(Texts.parse(null, title, null, 0)) );
            target.networkHandler.sendPacket(subtitleConstructor.apply(Texts.parse(null, subTitle, null, 0)) );
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
