package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.ArenaIO;
import com.strangeone101.platinumarenas.ConfigManager;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Section;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class InfoCommand extends ArenaCommand {

    public InfoCommand() {
        super("info", "Read information about an arena", "/arena info <arena>", new String[] {"display"});
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.info")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        if (args.size() == 0) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Command usage is /arena info <arena>");
            return;
        }

        if (!Arena.arenas.containsKey(args.get(0).toLowerCase())) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena by that name not found!");
            return;
        }

        Arena arena = Arena.arenas.get(args.get(0).toLowerCase());

        int x = arena.getCorner1().getBlockX() + (arena.getWidth() / 2);
        int y = arena.getCorner1().getBlockY() + (arena.getHeight() / 2);
        int z = arena.getCorner1().getBlockZ() + (arena.getLength() / 2);

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " === " + ChatColor.YELLOW + arena.getName() + ChatColor.RED + " ===");

        String s = ChatColor.YELLOW + "Dimensions: " + ChatColor.GRAY + arena.getWidth() + " x " + arena.getHeight() + " x " + arena.getLength()
                + "\n" + ChatColor.YELLOW + "Size: " + ChatColor.GRAY + NumberFormat.getInstance().format(arena.getTotalBlocks()) + " blocks" +
                "\n" + ChatColor.YELLOW + "Coords: " + ChatColor.GRAY + x + " " + y + " " + z +
                "\n" + ChatColor.YELLOW + "World: " + ChatColor.GRAY + arena.getCorner1().getWorld().getName();

        String owner = "(Unknown)";
        if (arena.hasOwner()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(arena.getCreator());
            if (player.getName() != null) owner = player.getName();
        }

        s = s + "\n" + ChatColor.YELLOW + "MC Version: " + ChatColor.GRAY + arena.getMcVersion();
        s = s + "\n" + ChatColor.YELLOW + "Creator: " + ChatColor.GRAY + owner;

        long time = arena.getCreationTime();
        String stringTime = "Before June 2022 ";
        if (time == 0) {
            File arenaFolder = new File(PlatinumArenas.INSTANCE.getDataFolder(), "Arenas");
            File maybeArena = new File(arenaFolder, arena.getName() + ".dat");

            if (maybeArena.exists()) {
                stringTime = stringTime + " (File last modified at " + getTime(maybeArena.lastModified()) + ")";
            }
        } else {
            stringTime = getTime(time);
        }


        s = s + "\n" + ChatColor.YELLOW + "Creation Time: " + ChatColor.GRAY + stringTime;
        s = s + "\n" + ChatColor.YELLOW + "Arena File Version: " + ChatColor.GRAY + arena.getFileVersion()
                + (arena.getFileVersion() == ArenaIO.FILE_VERSION ? " (LATEST)" : " (Latest is " + (ArenaIO.FILE_VERSION) + ")");
        s = s + "\n" + ChatColor.YELLOW + "Sections: " + ChatColor.GRAY + arena.getSections().size();
        s = s + "\n" + ChatColor.YELLOW + "Block Types: " + ChatColor.GRAY + arena.getKeys().length;
        s = s + "\n" + ChatColor.YELLOW + "Blocks with NBT: " + ChatColor.GRAY + arena.getSections().stream().map(Section::getNBTCache).mapToInt(Map::size).sum();

        for (String line : s.split("\n")) {
            sender.sendMessage(PlatinumArenas.PREFIX + " " + line);
        }
    }

    private String getTime(long time) {
        long relative = System.currentTimeMillis() - time;

        if (relative < 1000 * 60 * 60 * 24) { //Less than 24h ago
            if (relative < 1000 * 60 * 60) { //Less than 1h ago
                int mins = (int) (relative / (1000 * 60));

                return mins + " minutes ago";
            }
            int hours = (int) (relative / (1000 * 60 * 60));

            return hours + " hours ago";
        }
        return DateFormat.getDateInstance().format(Date.from(Instant.ofEpochMilli(time)));
    }

    @Override
    protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
        List<String> completions = new ArrayList<>();
        if (args.size() <= 1) {
            completions.addAll(Arena.arenas.keySet());
            completions.sort(Comparator.naturalOrder());
        }

        return completions;
    }
}
