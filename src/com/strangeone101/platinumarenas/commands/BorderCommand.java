package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Section;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorderCommand extends ArenaCommand {

    public static Map<Player, Arena> borders = new HashMap<>();

    public static final Material BORDER_1 = Material.LIME_WOOL;
    public static final Material BORDER_2 = Material.RED_WOOL;

    public BorderCommand() {
        super("border", "Show borders of an arena", "/arena border <arena>", new String[] {"showborder", "borders", "hideborders"});
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.list")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " This command must be run by players!");
            return;
        }

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        if (args.size() == 0) {
            if (borders.containsKey(sender)) {
                removePlayer(borders.get(sender), ((Player)sender));
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Border visibility disabled.");
                return;
            }
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You must provide an arena to show borders on!");
            return;
        }

        if (!Arena.arenas.containsKey(args.get(0).toLowerCase())) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena by that name not found!");
            return;
        }

        if (borders.containsKey(sender)) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Border visibility of arena \"" + borders.get(((Player)sender)).getName() + "\" disabled.") ;
            removePlayer(borders.get(sender), ((Player)sender));
            return;
        }

        Arena arena = Arena.arenas.get(args.get(0).toLowerCase());
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Showing the border of arena \"" + arena.getName() + "\"! Run /arena hideborders to disable it");
        sendBorder(arena, ((Player)sender), false);
        borders.put((Player)sender, arena);

        new BukkitRunnable() {

            @Override
            public void run() {
                removePlayer(arena, ((Player)sender));
            }
        }.runTaskLater(PlatinumArenas.INSTANCE, 20 * 60 * 2);
    }

    public static void removePlayer(Arena arena, Player player) {
        borders.remove(player);
        sendBorder(arena, player, true);
    }

    public static void sendBorder(Arena arena, Player player, boolean revert) {
        List<BukkitRunnable> runnables = new ArrayList<>();
        for (int i = 0; i < arena.getSections().size(); i++) {
            Section section = arena.getSections().get(i);
            final int x1 = section.getStart().getBlockX();
            final int x2 = section.getEnd().getBlockX();
            final int y1 = section.getStart().getBlockY();
            final int y2 = section.getEnd().getBlockY();
            final int z1 = section.getStart().getBlockZ();
            final int z2 = section.getEnd().getBlockZ();
            final int counter = i;

            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int x = x1; x <= x2; x++) {
                        for (int z = z1; z <= z2 ; z += z2-z1) {

                            Location loc = new Location(arena.getCorner1().getWorld(), x, y1, z);
                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                            loc = new Location(arena.getCorner1().getWorld(), x, y2, z);
                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                            try {
                                int y = loc.getWorld().getHighestBlockYAt(loc.clone());
                                if (y < y1 || y > y2) continue;
                                loc = new Location(arena.getCorner1().getWorld(), x, y, z);
                            } catch (NullPointerException e) {
                                continue;
                            }

                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                        }
                    }

                    for (int z = z1; z <= z2; z++) {
                        for (int x = x1; x <= x2; x += x2-x1) {
                            Location loc = new Location(arena.getCorner1().getWorld(), x, y1, z);
                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                            loc = new Location(arena.getCorner1().getWorld(), x, y2, z);
                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                            try {
                                int y = loc.getWorld().getHighestBlockYAt(loc.clone());
                                if (y < y1 || y > y2) continue;
                                loc = new Location(arena.getCorner1().getWorld(), x, y, z);
                            } catch (NullPointerException e) {
                                continue;
                            }
                            player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));
                        }
                    }

                    for (int y = y1; y <= y2; y++) {
                        Location loc = new Location(arena.getCorner1().getWorld(), x1, y, z1);
                        player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                        loc = new Location(arena.getCorner1().getWorld(), x2, y, z1);
                        player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                        loc = new Location(arena.getCorner1().getWorld(), x1, y, z2);
                        player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));

                        loc = new Location(arena.getCorner1().getWorld(), x2, y, z2);
                        player.sendBlockChange(loc, revert ? loc.getBlock().getBlockData() : (counter % 2 == 0 ? BORDER_1.createBlockData() : BORDER_2.createBlockData()));
                    }
                }
            };

            runnables.add(r);
        }

        for (int i = 0; i < runnables.size(); i++) {
            runnables.get(i).runTaskLater(PlatinumArenas.INSTANCE, i + 1);
        }
    }
}
