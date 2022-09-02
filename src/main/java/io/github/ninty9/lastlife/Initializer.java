package io.github.ninty9.lastlife;

import com.mojang.brigadier.Command;
import io.github.ninty9.lastlife.commands.RegisterCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.ninty9.lastlife.Config.config;
import static io.github.ninty9.lastlife.PlayerLivesList.*;

public class Initializer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("lastlife");
	Command<Object> genLives = context -> {
		return 0;
	};
	public static final Path livesPath = (Path) Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/players.json");
	public static final Path configPath = (Path) Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/config.json");
	public static final Path sessionPath = (Path) Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/session.json");

	public static MinecraftServer serverObject;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
		RegisterCommands.registerCommands();

		LOGGER.info(livesPath.toString());
		LoadLives();
		LoadConfig();
		LoadSession();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			serverObject = server;
			PlayerLivesList.SetTeamColors();
		});

		ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
			PlayerLivesList.RelativeChangeLives(player.getUuid(), -1);
			return ActionResult.SUCCESS.shouldIncrementStat();
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PlayerLivesList.UpdatePlayer(newPlayer);
			//todo: add flavour text
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (IsPlayerOnList(handler.getPlayer())) {
				UpdatePlayer(handler.getPlayer());
			}
			else {
				if (config.rollOnJoin)
				{
					PlayerLivesList.RollPlayer(handler.getPlayer());
				}
			}
			Sessions.addToJoinList(handler.getPlayer().getUuid());
		});
	}

	private void LoadLives()
	{
		File livesFile = new File("D:\\code\\java\\lastlife\\run\\config\\lastlife\\players.json");
		boolean livesResult;
		try
		{
			livesFile.getParentFile().mkdirs();
			livesResult = livesFile.createNewFile();  //creates a new file
			if(livesResult)      // test if successfully created a new file
				System.out.println("file created "+livesFile.getCanonicalPath()); //returns the path string
			else
			{
				System.out.println("File already exist at location: " + livesFile.getCanonicalPath());
				ReadToLivesList();
			}
		}
		catch (Exception e) { e.printStackTrace(); } //prints exception if any
	}

	private void LoadConfig()
	{
		File configFile = new File("D:\\code\\java\\lastlife\\run\\config\\lastlife\\config.json");
		boolean configResult;
		try
		{
			configFile.getParentFile().mkdirs();
			configResult = configFile.createNewFile();  //creates a new file
			if (configResult)    // test if successfully created a new file
			{
				System.out.println("file created " + configFile.getCanonicalPath()); //returns the path string
				config = new Config(2, 9, false, false);
				Config.UpdateConfigFile();
			} else
			{
				System.out.println("File already exist at location: " + configFile.getCanonicalPath());
				Config.ReadToConfig();
			}
		}
		catch (Exception e) { e.printStackTrace(); } //prints exception if any
	}

	private void LoadSession()
	{
		File sessionFile = new File("D:\\code\\java\\lastlife\\run\\config\\lastlife\\session.json");
		boolean result;
		try
		{
			sessionFile.getParentFile().mkdirs();
			result = sessionFile.createNewFile();  //creates a new file
			if(result)      // test if successfully created a new file
				System.out.println("file created "+sessionFile.getCanonicalPath()); //returns the path string
			else
			{
				System.out.println("File already exist at location: " + sessionFile.getCanonicalPath());
				ReadToLivesList();
			}
		}
		catch (Exception e) { e.printStackTrace(); } //prints exception if any
	}
}
