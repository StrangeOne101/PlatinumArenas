package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Section;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DebugCommand extends ArenaCommand {

    public static String debugString = "";

    public DebugCommand() {
        super("debug", "Debug things with this plugin", "/arena debug <arena>", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.debug")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (args.size() == 0) {
            if (!debugString.equals("")) {
                sender.sendMessage(debugString);
                return;
            }
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Use /arena debug <arena>");
            return;
        }

        if (!Arena.arenas.containsKey(args.get(0).toLowerCase())) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena not found! Use /arena list");
            return;
        }

        Arena arena = Arena.arenas.get(args.get(0).toLowerCase());

        TextComponent comma = new TextComponent(",");
        TextComponent sections = new TextComponent("");

        int i = 0;
        for (Section s : arena.getSections()) {
            TextComponent section = new TextComponent(i + "");
            section.setColor(i % 2 == 0 ? net.md_5.bungee.api.ChatColor.GRAY : net.md_5.bungee.api.ChatColor.WHITE);
            int chunksX = Math.abs(s.getEnd().getChunk().getX() - s.getStart().getChunk().getX()) + 1;
            int chunksZ = Math.abs(s.getEnd().getChunk().getZ() - s.getStart().getChunk().getZ()) + 1;
            //int blocks = s.getTotalBlocks();
            String hover = ChatColor.YELLOW + "Section " + i
                    + "\nChunksX: " + ChatColor.GRAY + chunksX + "\n" + ChatColor.YELLOW + "ChunksZ: " + ChatColor.GRAY + chunksZ
                    //+ "\n" + ChatColor.YELLOW + "Blocks: " + ChatColor.GRAY + blocks
                    + "\n" + ChatColor.YELLOW + "Width: " + ChatColor.GRAY + s.getWidth()
                    + "\n" + ChatColor.YELLOW + "Height: " + ChatColor.GRAY + s.getHeight()
                    + "\n" + ChatColor.YELLOW + "Length: " + ChatColor.GRAY + s.getLength();
            section.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
            sections.addExtra(section);
            sections.addExtra(comma);
            i++;
        }

        sender.sendMessage(ChatColor.YELLOW + "Debug for arena " + arena.getName());
        sender.spigot().sendMessage(sections);
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}