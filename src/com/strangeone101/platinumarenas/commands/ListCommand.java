package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends ArenaCommand {
    public ListCommand() {
        super("list", "Show a list of saved arenas", "/arenas list", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.list")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        List<String> arenas = new ArrayList<String>();

        for (Arena arena : Arena.arenas.values()) {
            int area = arena.getWidth() * arena.getHeight() * arena.getLength();
            String size = area < 2000 ? "Very Small" : (area < 10_000 ? "Small" : (area < 50_000 ? "Medium" : (area < 200_000 ? "Large" : (area < 2_000_000 ? "Very Large" : ("Insane")))));

            arenas.add(arena.getName() + " " + ChatColor.GREEN + " - " + ChatColor.YELLOW + arena.getWidth() + " x "
                    + arena.getHeight() + " x " + arena.getLength() + ChatColor.GREEN
                    + " (" + arena.getCorner1().getWorld().getName() + ChatColor.GREEN + ")");
        }

        int page = 1;
        if (args.size() > 0 && Util.isInteger(args.get(0))) {
            page = Integer.parseInt(args.get(0));
        }

        if (page < 1 || page > arenas.size() / 10 + 1) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Invalid page number!");
            return;
        }

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Listing all the arenas: ");
        for (int i = page * 10 - 10; i < page * 10 && i < arenas.size(); i++) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " > " + arenas.get(i));
        }

        if (arenas.size() > 10) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " To view the next page, type /arena list " + (page + 1));
        }
    }
}
