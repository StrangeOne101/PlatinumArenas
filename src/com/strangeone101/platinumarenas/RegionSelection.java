package com.strangeone101.platinumarenas;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RegionSelection {

    public static boolean hasWorldEdit() {
        return Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
    }

    public static boolean hasSelectedRegion(Player player) {
        if (hasWorldEdit()) {
            com.sk89q.worldedit.entity.Player pl = BukkitAdapter.adapt(player);
            com.sk89q.worldedit.world.World wld = BukkitAdapter.adapt(player.getWorld());

            try {
                if (WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld) != null) {
                    Region region = WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld);
                    Location corner1 = BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint());
                    Location corner2 = BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint());

                    List<Location> corners = new ArrayList<>();
                    corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner1.getBlockY(), corner2.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner2.getBlockY(), corner1.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner2.getBlockY(), corner2.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner1.getBlockY(), corner2.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner2.getBlockY(), corner1.getBlockZ()));
                    corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()));

                    //If the player is not within 100 blocks of either corner
                    for (Location corner : corners) {
                        if (corner.distanceSquared(player.getLocation()) < 100 * 100) {
                            return true;
                        }
                    }

                    return false;
                }
            } catch (IncompleteRegionException e) {
                return false;
            }
        } else { //Own region selection TODO

        }

        return false;
    }

    public static Block[] getRegionCorners(Player player) {
        if (hasWorldEdit()) {
            com.sk89q.worldedit.entity.Player pl = BukkitAdapter.adapt(player);
            com.sk89q.worldedit.world.World wld = BukkitAdapter.adapt(player.getWorld());

            try {
                if (WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld) != null) {
                    Region region = WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld);
                    Location corner1 = BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint());
                    Location corner2 = BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint());

                    return new Block[] {corner1.getBlock(), corner2.getBlock()};
                }
            } catch (IncompleteRegionException e) {
                return null;
            }
        }
        return null;
    }

}
