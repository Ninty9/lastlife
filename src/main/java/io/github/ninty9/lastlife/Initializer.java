package io.github.ninty9.lastlife;

import com.google.gson.Gson;
import com.mojang.authlib.yggdrasil.response.User;
import com.mojang.brigadier.Command;
import io.github.ninty9.lastlife.commands.GetLives;
import io.github.ninty9.lastlife.commands.RegisterCommands;
import io.github.ninty9.lastlife.commands.RollLives;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.ninty9.lastlife.PlayerLivesList.playerLivesList;

public class Initializer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("lastlife");
	Command<Object> genLives = context -> {
		return 0;
	};
	public static final Path configPath = (Path) Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/players.json") ;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
		CommandRegistrationCallback.EVENT.register(RollLives::register);
		CommandRegistrationCallback.EVENT.register(GetLives::register);

		LOGGER.info(configPath.toString());
		File configFile = new File("D:\\code\\java\\lastlife\\run\\config\\lastlife\\players.json");
		boolean result1, result2;
		try
		{
			result1 = configFile.getParentFile().mkdirs();
			result2 = configFile.createNewFile();  //creates a new file
			if(result1 && result2)      // test if successfully created a new file
			{
				System.out.println("file created "+configFile.getCanonicalPath()); //returns the path string
			}
			else
			{
				System.out.println("File already exist at location: "+configFile.getCanonicalPath());
			}
			Gson gson = new Gson();
			Reader reader = Files.newBufferedReader(configPath);
			List<PlayerLives>tempList = Arrays.asList(gson.fromJson(reader, PlayerLives[].class));
			reader.close();
			for (PlayerLives p: tempList)
				PlayerLivesList.AddToList(p);
			LOGGER.info(playerLivesList.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();    //prints exception if any
		}
	}
}
