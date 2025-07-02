package com.harismehuljic.wyvern;

import com.harismehuljic.wyvern.config.ConfigData;
import com.harismehuljic.wyvern.config.ConfigManager;
import com.harismehuljic.wyvern.discord.DiscordBot;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Wyvern implements ModInitializer {
	public static final String MOD_ID = "wyvern";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();

	public static ConfigData CONFIG_DATA;
	public static MinecraftServer SERVER;
	public static DiscordBot DISCORD_BOT = new DiscordBot(List.of("ping.json", "players.json", "title.json", "tps.json"));

	@Override
	public void onInitialize() {
		LOGGER.info("Miku SMP Utils version {} loading.", VERSION);

		CONFIG_DATA = ConfigManager.loadConfig();

		DISCORD_BOT.initialize();
		Registries.registerAllRegistries();

		LOGGER.info("Miku SMP Utils successfully loaded!");
	}
}
