package com.harismehuljic.wyvern.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.harismehuljic.wyvern.Wyvern;
import net.fabricmc.loader.api.FabricLoader;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    @Nullable
    public static ConfigData loadConfig() {
        try {
            Gson gson = new Gson();

            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), Wyvern.MOD_ID + ".json");
            ConfigData configData = gson.fromJson(new FileReader(configFile), ConfigData.class);

            return configData;
        }
        catch (FileNotFoundException e) {
            Wyvern.LOGGER.error("Looks like the config file is missing. Regenerating default config.");
            return generateConfig();
        }
        catch (Exception e) {
            Wyvern.LOGGER.error("Something went wrong when reading the config: {}", e.getMessage());
            return null;
        }
    }

    private static ConfigData generateConfig() {
        ConfigData configData = new ConfigData();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), Wyvern.MOD_ID + ".json");

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configData, writer);
            return configData;
        } catch (Exception exception) {
            Wyvern.LOGGER.error("Couldn't generate default config: {}", exception.getMessage());
            return null;
        }
    }
}
