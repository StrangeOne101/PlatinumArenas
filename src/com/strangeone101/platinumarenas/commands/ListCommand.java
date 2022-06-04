package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.ArenaIO;
import com.strangeone101.platinumarenas.ConfigManager;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Util;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
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

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        List<TextComponent> arenas = new ArrayList<TextComponent>();

        for (Arena arena : Arena.arenas.values()) {
            String size = arena.getStringSize(); //Very small, large, very large, etc

            int x = arena.getCorner1().getBlockX() + (arena.getWidth() / 2);
            int y = arena.getCorner1().getBlockY() + (arena.getHeight() / 2);
            int z = arena.getCorner1().getBlockZ() + (arena.getLength() / 2);

            String outdated = arena.getFileVersion() == ArenaIO.FILE_VERSION ? "" : ChatColor.RED + "*" + ChatColor.YELLOW;

            String string = ChatColor.GREEN + " - " + ChatColor.YELLOW + arena.getName() + outdated + " (" + size + ")";

            TextComponent arenaComponent = new TextComponent("");
            for (BaseComponent c : TextComponent.fromLegacyText(PlatinumArenas.PREFIX + string)) arenaComponent.addExtra(c);
            arenaComponent.setColor(net.md_5.bungee.api.ChatColor.RED);
            String s = ChatColor.YELLOW + "Dimensions: " + ChatColor.GRAY + arena.getWidth() + " x " + arena.getHeight() + " x " + arena.getLength()
                    + "\n" + ChatColor.YELLOW + "Size: " + ChatColor.GRAY + NumberFormat.getInstance().format(arena.getTotalBlocks()) + " blocks" +
                    "\n" + ChatColor.YELLOW + "Coords: " + ChatColor.GRAY + x + " " + y + " " + z +
                    "\n" + ChatColor.YELLOW + "World: " + ChatColor.GRAY + arena.getCorner1().getWorld().getName() + "\n\n" +
                    ChatColor.YELLOW + "Click to teleport to the arena!";

            if (arena.getFileVersion() != ArenaIO.FILE_VERSION) {
                s = s + "\n\n" + ChatColor.RED + "This arena was made with an older\n" + ChatColor.RED + "version of PlatinumArenas.";
            }
            arenaComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(s)));
            arenaComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTeleport(arena, sender)));

            arenas.add(arenaComponent);
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
            //sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " - " + arenas.get(i));
            sender.spigot().sendMessage(arenas.get(i));
        }

        if (page + 1 <= arenas.size() / 10 + 1) { //If there is a next page
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " To view the next page, type /arena list " + (page + 1));
        }
    }

    public static String getTeleport(Arena arena, CommandSender sender) {
        int x = arena.getCorner1().getBlockX() + (arena.getWidth() / 2);
        int y = arena.getCorner1().getBlockY() + (arena.getHeight() / 2);
        int z = arena.getCorner1().getBlockZ() + (arena.getLength() / 2);

        return ConfigManager.TELEPORT_COMMAND.replace("<x>", x + "")
                .replace("<y>", y + "")
                .replace("<z>", z + "")
                .replace("<player>", sender.getName())
                .replace("<world>", arena.getCorner1().getWorld().getName());
    }
}
