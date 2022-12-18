package io.github.ninty9.lastlife;

import io.github.ninty9.lastlife.commands.RegisterCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.github.ninty9.lastlife.Config.*;
import static io.github.ninty9.lastlife.PlayerLivesList.*;
import static io.github.ninty9.lastlife.Sessions.ReadToSession;

public class Initializer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("lastlife");
	public static final Path livesPath = Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/players.json");
	public static final Path configPath = Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/config.json");
	public static final Path sessionPath = Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/session.json");

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
			PlayerLivesList.UpdatePlayer(newPlayer, true);
			DisplayLivesMessage(newPlayer, true);
		});

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
			if (
					killedEntity instanceof ServerPlayerEntity &&
							entity instanceof ServerPlayerEntity killer &&
							Objects.equals(killer.getUuid(), config.boogeyman)
			) {
				config.boogeyman = null;
				UpdateConfigFile();
			}

		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (IsPlayerOnList(player)) {
				UpdatePlayer(player, false);
			} else {
				if (config.rollOnJoin) {
					PlayerLivesList.RollPlayer(player);
					DisplayLivesMessage(player, false);
				}
			}
			Sessions.addToJoinList(player.getUuid());
		});
	}



	private void LoadLives()
	{
		File livesFile = new File(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/players.json");
		CreateOrReadFile(livesFile, false);
		ReadToLivesList();
	}

	private void LoadConfig()
	{
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/config.json");
		CreateOrReadFile(configFile ,true);
		ReadToConfig();
	}

	private void LoadSession()
	{
		File sessionFile = new File(FabricLoader.getInstance().getConfigDir().toString() + "/lastlife" + "/session.json");
		CreateOrReadFile(sessionFile ,false);
		ReadToSession();
	}

	private void CreateOrReadFile(File file, Boolean isConfig) {
		boolean result;
		try
		{
			file.getParentFile().mkdirs();
			result = file.createNewFile();  //creates a new file
			if(result)      // test if successfully created a new file
			{
				System.out.println("file created " + file.getCanonicalPath()); //returns the path string
				if (isConfig)
				{
					config = new Config(2, 9, false, false, null);
					Config.UpdateConfigFile();
				}
			}
		}
		catch(IOException ignored) {}
	}
}
