package com.shroototem.pipez.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pipez configuration backed by a JSON file at config/pipez.json.
 * Replaces the NeoForge ServerConfig / ModConfigSpec approach with a simple GSON-based config.
 */
public class PipezConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("pipez");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Item pipe settings
    public static int itemPipeSpeed = 20;
    public static int itemPipeAmount = 4;
    public static int itemPipeSpeedBasic = 15;
    public static int itemPipeAmountBasic = 8;
    public static int itemPipeSpeedImproved = 10;
    public static int itemPipeAmountImproved = 16;
    public static int itemPipeSpeedAdvanced = 5;
    public static int itemPipeAmountAdvanced = 32;
    public static int itemPipeSpeedUltimate = 1;
    public static int itemPipeAmountUltimate = 64;

    // Fluid pipe settings (mB per tick)
    public static int fluidPipeAmount = 50;
    public static int fluidPipeAmountBasic = 100;
    public static int fluidPipeAmountImproved = 500;
    public static int fluidPipeAmountAdvanced = 2000;
    public static int fluidPipeAmountUltimate = 10000;

    // Energy pipe settings (FE per tick)
    public static int energyPipeAmount = 256;
    public static int energyPipeAmountBasic = 1024;
    public static int energyPipeAmountImproved = 8192;
    public static int energyPipeAmountAdvanced = 32768;
    public static int energyPipeAmountUltimate = 131072;

    // Gas pipe settings (mB per tick, only relevant if Mekanism is installed)
    public static int gasPipeAmount = 200;
    public static int gasPipeAmountBasic = 400;
    public static int gasPipeAmountImproved = 2000;
    public static int gasPipeAmountAdvanced = 8000;
    public static int gasPipeAmountUltimate = 40000;

    /**
     * Loads the configuration from config/pipez.json.
     * If the file does not exist, it is created with default values.
     * If the file exists but is missing fields, those fields keep their defaults.
     */
    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve("pipez.json");

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    applyFromData(data);
                }
                LOGGER.info("Loaded Pipez config from {}", configFile);
            } catch (Exception e) {
                LOGGER.error("Failed to load Pipez config, using defaults", e);
            }
        } else {
            LOGGER.info("Pipez config not found, creating default at {}", configFile);
        }

        // Always save to ensure the file exists and any new fields are written
        save(configFile);
    }

    private static void save(Path configFile) {
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(toData(), writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save Pipez config", e);
        }
    }

    private static void applyFromData(ConfigData data) {
        itemPipeSpeed = data.itemPipeSpeed;
        itemPipeAmount = data.itemPipeAmount;
        itemPipeSpeedBasic = data.itemPipeSpeedBasic;
        itemPipeAmountBasic = data.itemPipeAmountBasic;
        itemPipeSpeedImproved = data.itemPipeSpeedImproved;
        itemPipeAmountImproved = data.itemPipeAmountImproved;
        itemPipeSpeedAdvanced = data.itemPipeSpeedAdvanced;
        itemPipeAmountAdvanced = data.itemPipeAmountAdvanced;
        itemPipeSpeedUltimate = data.itemPipeSpeedUltimate;
        itemPipeAmountUltimate = data.itemPipeAmountUltimate;

        fluidPipeAmount = data.fluidPipeAmount;
        fluidPipeAmountBasic = data.fluidPipeAmountBasic;
        fluidPipeAmountImproved = data.fluidPipeAmountImproved;
        fluidPipeAmountAdvanced = data.fluidPipeAmountAdvanced;
        fluidPipeAmountUltimate = data.fluidPipeAmountUltimate;

        energyPipeAmount = data.energyPipeAmount;
        energyPipeAmountBasic = data.energyPipeAmountBasic;
        energyPipeAmountImproved = data.energyPipeAmountImproved;
        energyPipeAmountAdvanced = data.energyPipeAmountAdvanced;
        energyPipeAmountUltimate = data.energyPipeAmountUltimate;

        gasPipeAmount = data.gasPipeAmount;
        gasPipeAmountBasic = data.gasPipeAmountBasic;
        gasPipeAmountImproved = data.gasPipeAmountImproved;
        gasPipeAmountAdvanced = data.gasPipeAmountAdvanced;
        gasPipeAmountUltimate = data.gasPipeAmountUltimate;
    }

    private static ConfigData toData() {
        ConfigData data = new ConfigData();
        data.itemPipeSpeed = itemPipeSpeed;
        data.itemPipeAmount = itemPipeAmount;
        data.itemPipeSpeedBasic = itemPipeSpeedBasic;
        data.itemPipeAmountBasic = itemPipeAmountBasic;
        data.itemPipeSpeedImproved = itemPipeSpeedImproved;
        data.itemPipeAmountImproved = itemPipeAmountImproved;
        data.itemPipeSpeedAdvanced = itemPipeSpeedAdvanced;
        data.itemPipeAmountAdvanced = itemPipeAmountAdvanced;
        data.itemPipeSpeedUltimate = itemPipeSpeedUltimate;
        data.itemPipeAmountUltimate = itemPipeAmountUltimate;

        data.fluidPipeAmount = fluidPipeAmount;
        data.fluidPipeAmountBasic = fluidPipeAmountBasic;
        data.fluidPipeAmountImproved = fluidPipeAmountImproved;
        data.fluidPipeAmountAdvanced = fluidPipeAmountAdvanced;
        data.fluidPipeAmountUltimate = fluidPipeAmountUltimate;

        data.energyPipeAmount = energyPipeAmount;
        data.energyPipeAmountBasic = energyPipeAmountBasic;
        data.energyPipeAmountImproved = energyPipeAmountImproved;
        data.energyPipeAmountAdvanced = energyPipeAmountAdvanced;
        data.energyPipeAmountUltimate = energyPipeAmountUltimate;

        data.gasPipeAmount = gasPipeAmount;
        data.gasPipeAmountBasic = gasPipeAmountBasic;
        data.gasPipeAmountImproved = gasPipeAmountImproved;
        data.gasPipeAmountAdvanced = gasPipeAmountAdvanced;
        data.gasPipeAmountUltimate = gasPipeAmountUltimate;
        return data;
    }

    /**
     * Internal POJO for GSON serialization/deserialization.
     * Field defaults match the NeoForge ServerConfig defaults.
     */
    private static class ConfigData {
        // Item pipe
        int itemPipeSpeed = 20;
        int itemPipeAmount = 4;
        int itemPipeSpeedBasic = 15;
        int itemPipeAmountBasic = 8;
        int itemPipeSpeedImproved = 10;
        int itemPipeAmountImproved = 16;
        int itemPipeSpeedAdvanced = 5;
        int itemPipeAmountAdvanced = 32;
        int itemPipeSpeedUltimate = 1;
        int itemPipeAmountUltimate = 64;

        // Fluid pipe
        int fluidPipeAmount = 50;
        int fluidPipeAmountBasic = 100;
        int fluidPipeAmountImproved = 500;
        int fluidPipeAmountAdvanced = 2000;
        int fluidPipeAmountUltimate = 10000;

        // Energy pipe
        int energyPipeAmount = 256;
        int energyPipeAmountBasic = 1024;
        int energyPipeAmountImproved = 8192;
        int energyPipeAmountAdvanced = 32768;
        int energyPipeAmountUltimate = 131072;

        // Gas pipe
        int gasPipeAmount = 200;
        int gasPipeAmountBasic = 400;
        int gasPipeAmountImproved = 2000;
        int gasPipeAmountAdvanced = 8000;
        int gasPipeAmountUltimate = 40000;
    }

}
