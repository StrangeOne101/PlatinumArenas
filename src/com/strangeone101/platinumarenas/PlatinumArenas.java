package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.blockentity.WrapperRegistry;
import com.strangeone101.platinumarenas.region.DefaultRegionSelection;
import com.strangeone101.platinumarenas.region.IRegionSelection;
import com.strangeone101.platinumarenas.region.WorldEditRegionSelection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PlatinumArenas extends JavaPlugin {

    public static PlatinumArenas INSTANCE;

    public static final String PREFIX = ChatColor.RED + "[" + ChatColor.GRAY + "PlatinumArenas" + ChatColor.RED + "]";

    public static final boolean DEBUG = false;

    public static UUID DEFAULT_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private IRegionSelection regionSelection;

    protected boolean ready;

    @Override
    public void onEnable() {
        INSTANCE = this;

        WrapperRegistry.registerAll();

        ArenaCommand.createCommands();
        getCommand("platinumarenas").setExecutor(ArenaCommand.getCommandExecutor());
        getCommand("platinumarenas").setTabCompleter(ArenaCommand.getTabCompleter());
        Bukkit.getPluginManager().registerEvents(new ArenaListener(), this);

        File folder = new File(getDataFolder(), "Arenas");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!ConfigManager.setup()) {
            getLogger().warning("Internal defaults will be used due to config not being loaded!");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            regionSelection = new WorldEditRegionSelection();
        } else {
            regionSelection = new DefaultRegionSelection();
        }

        getLogger().info("PlatinumArenas Enabled!");
        getLogger().info("Loading arenas... this will be done async.");

        async(ArenaIO::loadAllArenas);
    }

    /**
     * Call method async
     * @param callable Method
     */
    public static void async(Callable<?> callable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(PlatinumArenas.INSTANCE);
    }

    public IRegionSelection getRegionSelection() {
        return regionSelection;
    }

    /**
     * Whether the plugin is ready to use arenas.
     * @return True when all arenas have been loaded
     */
    public boolean isReady() {
        return ready;
    }

    public static String getMCVersion() {
        return Bukkit.getBukkitVersion().split("-", 2)[0];
    }

    public static int getIntVersion(String version) {

        if (!version.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            PlatinumArenas.INSTANCE.getLogger().warning("Version not valid! Cannot parse version \"" + version + "\"");

            return 1164; //1.16.4
        }

        String[] split = version.split("\\.", 3);

        int major = Integer.parseInt(split[0]);
        int minor = 0;
        int fix = 0;

        if (split.length > 1) {
            minor = Integer.parseInt(split[1]);

            if (split.length > 2) {
                fix = Integer.parseInt(split[2]);
            }
        }

        return major * 1000 + minor * 10 + fix; //1.16.4 -> 1164; 1.18 -> 1180
    }

    public static int getMCVersionInt() {
        return getIntVersion(getMCVersion());
    }

    public static void debug(String string) {
        if (DEBUG) {
            PlatinumArenas.INSTANCE.getLogger().info(string);
        }
    }
}
