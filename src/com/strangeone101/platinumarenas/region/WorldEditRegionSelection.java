package com.strangeone101.platinumarenas.region;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldEditRegionSelection extends IRegionSelection {

    @Override
    public boolean hasSelectedRegion(Player player) {
        com.sk89q.worldedit.entity.Player pl = BukkitAdapter.adapt(player);
        com.sk89q.worldedit.world.World wld = BukkitAdapter.adapt(player.getWorld());

        try {
            if (WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld) != null) {
                Region region = WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld);
                Location corner1 = BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint());
                Location corner2 = BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint());

                List<Location> corners = getCorners(corner1, corner2);

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
        return false;
    }

    @Override
    public Block[] getRegionCorners(Player player) {
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
        return null;
    }

    @Override
    public Material getWand() {
        return Material.matchMaterial(WorldEdit.getInstance().getConfiguration().wandItem) == null ? Material.WOODEN_AXE : Material.matchMaterial(WorldEdit.getInstance().getConfiguration().wandItem);
    }
}
