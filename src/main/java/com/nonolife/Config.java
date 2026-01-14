package com.nonolife;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

import net.minecraft.server.MinecraftServer;

class Config {
    int dailyPlaytime = 3600;
    int initialPlaytime = 3600;
    int maximumPlaytime = 3600 * 4;
    int addPlaytimeAtHour = 4;
    boolean showTotalPlaytime = true;

    public static Config load(MinecraftServer server) {
        Path configFile = server.getServerDirectory().resolve("config").resolve("nonolife.json");

        if (!Files.exists(configFile)) {
            NoNoLife.LOGGER.info("No nonolive config file found. Using default config...");
            return new Config();
        }

        try {
            String json = Files.readString(configFile);
            Gson gson = new Gson();
            return gson.fromJson(json, Config.class);
        } catch (IOException e) {
            NoNoLife.LOGGER.error("Could not read from config/nonolive.json. Using default config...");
            return new Config();
        }
    }
}
