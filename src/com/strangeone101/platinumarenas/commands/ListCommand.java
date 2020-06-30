package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
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
    }
}
