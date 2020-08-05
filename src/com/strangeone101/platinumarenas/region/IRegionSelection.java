package com.strangeone101.platinumarenas.region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IRegionSelection {

    public abstract boolean hasSelectedRegion(Player player);

    public abstract Block[] getRegionCorners(Player player);

    public abstract Material getWand();

    public static List<Location> getCorners(Location corner1, Location corner2) {
        List<Location> corners = new ArrayList<>();
        corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner1.getBlockY(), corner2.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner2.getBlockY(), corner1.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner2.getBlockY(), corner2.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner1.getBlockY(), corner2.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner2.getBlockX(), corner2.getBlockY(), corner1.getBlockZ()));
        corners.add(new Location(corner1.getWorld(), corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()));
        return corners;
    }
}
