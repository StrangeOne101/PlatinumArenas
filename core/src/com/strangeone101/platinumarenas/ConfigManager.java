package com.strangeone101.platinumarenas;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    /**
     * How many blocks to analyze/count before waiting a bit before continuing.
     */
    public static int BLOCKS_ANALYZED_PER_SECOND = 49_600;

    /**
     * How many blocks can be per section in arenas. Max is 2,147,483,647
     */
    public static int BLOCKS_PER_SECTION = 28_800;

    public static int BLOCKS_RESET_PER_SECOND_VERYSLOW = 10 * 20;
    public static int BLOCKS_RESET_PER_SECOND_SLOW = 50 * 20;
    public static int BLOCKS_RESET_PER_SECOND_NORMAL = 500 * 20;
    public static int BLOCKS_RESET_PER_SECOND_FAST = 2000 * 20;
    public static int BLOCKS_RESET_PER_SECOND_VERYFAST = 5000 * 20;
    public static int BLOCKS_RESET_PER_SECOND_EXTREME = 10000 * 20;

    public static int RESET_UPDATE_INTERVAL = 10;
    public static float RESET_UPDATE_PERCENTAGE = 5F;

    public static boolean ENABLE_COMPRESSION = true;

    public static String TELEPORT_COMMAND = "/tp <x> <y> <z>";

    public static boolean IGNORE_OUTDATED_MATERIALS = false;

    private static YamlConfiguration config;

    public static boolean setup() {
        File file = new File(PlatinumArenas.INSTANCE.getDataFolder(), "config.yml");
        if (!file.exists()) {
            if (!Util.saveResource("config.yml", file)) {
                PlatinumArenas.INSTANCE.getLogger().severe("Failed to copy default config!");
                return false;
            }
        }
        config = new YamlConfiguration();
        try {
            config.load(file);

            BLOCKS_ANALYZED_PER_SECOND = config.getInt("AnalyzeBlockSpeed", BLOCKS_ANALYZED_PER_SECOND);

            BLOCKS_PER_SECTION = config.getInt("MaxSectionSize", BLOCKS_PER_SECTION);

            BLOCKS_RESET_PER_SECOND_VERYSLOW = config.getInt("Speeds.VerySlow", BLOCKS_RESET_PER_SECOND_VERYSLOW);
            BLOCKS_RESET_PER_SECOND_SLOW = config.getInt("Speeds.Slow", BLOCKS_RESET_PER_SECOND_SLOW);
            BLOCKS_RESET_PER_SECOND_NORMAL = config.getInt("Speeds.Normal", BLOCKS_RESET_PER_SECOND_NORMAL);
            BLOCKS_RESET_PER_SECOND_FAST = config.getInt("Speeds.Fast", BLOCKS_RESET_PER_SECOND_FAST);
            BLOCKS_RESET_PER_SECOND_VERYFAST = config.getInt("Speeds.VeryFast", BLOCKS_RESET_PER_SECOND_VERYFAST);
            BLOCKS_RESET_PER_SECOND_EXTREME = config.getInt("Speeds.Extreme", BLOCKS_RESET_PER_SECOND_EXTREME);

            RESET_UPDATE_INTERVAL = config.getInt("ResetUpdate.Interval", RESET_UPDATE_INTERVAL);
            RESET_UPDATE_PERCENTAGE = (float)config.getDouble("ResetUpdate.Percent", RESET_UPDATE_PERCENTAGE);

            ENABLE_COMPRESSION = !config.getBoolean("DisableCompression", !ENABLE_COMPRESSION);

            TELEPORT_COMMAND = config.getString("TeleportCommandSuggestion", TELEPORT_COMMAND);

            IGNORE_OUTDATED_MATERIALS = config.getBoolean("IgnoreOutdatedMaterials", IGNORE_OUTDATED_MATERIALS);

            if (RESET_UPDATE_INTERVAL < 0) RESET_UPDATE_INTERVAL = 1;
            if (RESET_UPDATE_PERCENTAGE > 100) RESET_UPDATE_PERCENTAGE = 100F;

            return true;
        } catch (IOException e) {
            PlatinumArenas.INSTANCE.getLogger().severe("Failed to load config.yml!");
            e.printStackTrace();
            return false;
        } catch (InvalidConfigurationException e) {
            PlatinumArenas.INSTANCE.getLogger().severe("Invalid YML format used in config.yml!");
            e.printStackTrace();
            return false;
        }
    }
}
