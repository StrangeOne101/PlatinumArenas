package com.strangeone101.platinumarenas.region;

import com.strangeone101.platinumarenas.PlatinumArenas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRegionSelection extends IRegionSelection implements Listener {

    protected Map<Player, Block> corners_1 = new HashMap<>();
    protected Map<Player, Block> corners_2 = new HashMap<>();

    public DefaultRegionSelection() {
        Bukkit.getPluginManager().registerEvents(this, PlatinumArenas.INSTANCE);
    }

    @Override
    public boolean hasSelectedRegion(Player player) {
        if (corners_1.containsKey(player) && corners_2.containsKey(player)) {
            Block corner1 = corners_1.get(player);
            Block corner2 = corners_2.get(player);

            if (corner1.getWorld() != corner2.getWorld()) return false;

            List<Location> corners = getCorners(corner1.getLocation(), corner2.getLocation());

            //If the player is not within 100 blocks of either corner
            for (Location corner : corners) {
                if (corner.distanceSquared(player.getLocation()) < 100 * 100) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Block[] getRegionCorners(Player player) {
        if (corners_1.containsKey(player) && corners_2.containsKey(player)) {
            return new Block[] {corners_1.get(player), corners_2.get(player)};
        }
        return null;
    }

    @Override
    public Material getWand() {
        return Material.STICK;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission("platinumarenas.create") && event.getHand() == EquipmentSlot.HAND
                && event.getPlayer().getInventory().getItemInMainHand().getType() == getWand()) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (corners_1.containsKey(event.getPlayer())) event.getPlayer().sendBlockChange(corners_1.get(event.getPlayer()).getLocation(), corners_1.get(event.getPlayer()).getBlockData());
                corners_1.put(event.getPlayer(), event.getClickedBlock());
                String s = PlatinumArenas.PREFIX + " " + ChatColor.YELLOW + "First position selected (" + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ() + ")";
                event.getPlayer().sendMessage(s);
                event.getPlayer().sendBlockChange(event.getClickedBlock().getLocation(), Material.GLOWSTONE.createBlockData());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().sendBlockChange(event.getClickedBlock().getLocation(), event.getClickedBlock().getBlockData());
                    }}.runTaskLater(PlatinumArenas.INSTANCE, 20 * 10);
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (corners_2.containsKey(event.getPlayer())) event.getPlayer().sendBlockChange(corners_2.get(event.getPlayer()).getLocation(), corners_2.get(event.getPlayer()).getBlockData());
                corners_2.put(event.getPlayer(), event.getClickedBlock());
                String s = PlatinumArenas.PREFIX + " " + ChatColor.YELLOW + "Second position selected (" + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ() + ")";
                event.getPlayer().sendMessage(s);
                event.getPlayer().sendBlockChange(event.getClickedBlock().getLocation(), Material.GLOWSTONE.createBlockData());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().sendBlockChange(event.getClickedBlock().getLocation(), event.getClickedBlock().getBlockData());
                    }}.runTaskLater(PlatinumArenas.INSTANCE, 20 * 10);
                event.setCancelled(true);
            }
        }
    }
}
