package com.strangeone101.platinumarenas;

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

    public static UUID DEFAULT_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private IRegionSelection regionSelection;

    @Override
    public void onEnable() {
        INSTANCE = this;

        getLogger().info("PlatinumArenas Enabled!");
        getLogger().info("Loading arenas... this will be done async.");
        async(ArenaIO::loadAllArenas);


        ArenaCommand.createCommands();
        getCommand("platinumarenas").setExecutor(ArenaCommand.getCommandExecutor());
        getCommand("platinumarenas").setTabCompleter(ArenaCommand.getTabCompleter());

        File folder = new File(getDataFolder(), "Arenas");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            regionSelection = new WorldEditRegionSelection();
        } else {
            regionSelection = new DefaultRegionSelection();
        }


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
}
