package io.github.ninty9.lastlife;

import com.mojang.brigadier.Command;
import io.github.ninty9.lastlife.commands.GetLives;
import io.github.ninty9.lastlife.commands.RegisterCommands;
import io.github.ninty9.lastlife.commands.RollLives;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initializer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("lastlife");
	Command<Object> genLives = context -> {
		return 0;
	};

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
		CommandRegistrationCallback.EVENT.register(RollLives::register);
		CommandRegistrationCallback.EVENT.register(GetLives::register);
	}
}
