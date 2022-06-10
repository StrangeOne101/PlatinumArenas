package com.strangeone101.platinumarenas.commands;

import com.google.common.io.Files;
import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Section;
import com.strangeone101.platinumarenas.blockentity.Wrapper;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class DebugCommand extends ArenaCommand {

    public static Map<UUID, Consumer<Block>> BLOCK_LISTENERS = new HashMap<>();

    public static String debugString;

    public DebugCommand() {
        super("debug", "Debug things with this plugin", "/arena debug <arena>", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.debug")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        if (args.size() == 0) {
            if (!debugString.equals("")) {
                sender.sendMessage(debugString);
                return;
            }
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Use /arena debug <...>");
            return;
        }

        /*if (!Arena.arenas.containsKey(args.get(0).toLowerCase())) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena not found! Use /arena list");
            return;
        }*/

        if (args.get(0).equalsIgnoreCase("arena")) {
            if (args.size() == 1) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Use /arena debug arena <arena> [chat/dump]");
                return;
            }

            if (!Arena.arenas.containsKey(args.get(1).toLowerCase())) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena not found! Use /arena list");
                return;
            }

            Arena arena = Arena.arenas.get(args.get(1).toLowerCase());

            String option = "chat";

            if (args.size() > 2) {
                option = args.get(2);
            }

            if (option.equalsIgnoreCase("chat")) {
                TextComponent comma = new TextComponent(",");
                TextComponent sections = new TextComponent("");

                for (Section s : arena.getSections()) {
                    TextComponent section = new TextComponent(s.getID() + "");
                    section.setColor(s.getID() % 2 == 0 ? net.md_5.bungee.api.ChatColor.GRAY : net.md_5.bungee.api.ChatColor.WHITE);
                    int chunksX = Math.abs(s.getEnd().getChunk().getX() - s.getStart().getChunk().getX()) + 1;
                    int chunksZ = Math.abs(s.getEnd().getChunk().getZ() - s.getStart().getChunk().getZ()) + 1;
                    int blocks = s.getTotalBlocks();
                    String hover = ChatColor.YELLOW + "Section " + s.getID()
                            + "\nChunksX: " + ChatColor.GRAY + chunksX + "\n" + ChatColor.YELLOW + "ChunksZ: " + ChatColor.GRAY + chunksZ
                            + "\n" + ChatColor.YELLOW + "Blocks: " + ChatColor.GRAY + blocks
                            + "\n" + ChatColor.YELLOW + "Width: " + ChatColor.GRAY + s.getWidth()
                            + "\n" + ChatColor.YELLOW + "Height: " + ChatColor.GRAY + s.getHeight()
                            + "\n" + ChatColor.YELLOW + "Length: " + ChatColor.GRAY + s.getLength()
                            + "\n" + ChatColor.YELLOW + "NBT Blocks: " + ChatColor.GRAY + s.getNBTCache().size();
                    section.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
                    sections.addExtra(section);
                    sections.addExtra(comma);
                }

                sender.sendMessage(ChatColor.YELLOW + "Debug for arena " + arena.getName());
                sender.spigot().sendMessage(sections);
            } else if (option.equalsIgnoreCase("dump") || option.equalsIgnoreCase("file")) {
                List<String> lines = new ArrayList<>();

                lines.add(" ======= " + arena.getName() + " ======= ");
                lines.add("Corner 1: " + arena.getCorner1().toString());
                lines.add("Corner 2: " + arena.getCorner2().toString());
                lines.add("Version: " + arena.getFileVersion());
                lines.add("UUID: " + (arena.hasOwner() ? arena.getCreator() : "None"));
                lines.add("MC: " + arena.getMcVersion());
                lines.add("Time: " + (arena.getCreationTime() == 0L ? "Unknown" : InfoCommand.getTime(arena.getCreationTime())));
                lines.add("Height: " + arena.getHeight());
                lines.add("Width: " + arena.getWidth());
                lines.add("Length: " + arena.getLength());
                lines.add("Total Area: " + arena.getTotalBlocks() + " blocks");
                lines.add("NBT Blocks: " + arena.getSections().stream().mapToInt(s -> s.getNBTCache().size()).sum());
                lines.add("Sections: " + arena.getSections());

                for (Section s : arena.getSections()) {
                    lines.add(" === Section ID " + s.getID() + " === ");
                    lines.addAll(getSectionLines(arena, s));
                    lines.add(":::End section");
                }

                byte[] bytes = String.join("\n", lines).getBytes(StandardCharsets.US_ASCII);
                File file = new File(PlatinumArenas.INSTANCE.getDataFolder(), "debug_arena_" + arena.getName() + ".txt");
                try {
                    Files.write(bytes, file);
                    sender.sendMessage(ChatColor.GREEN + "File " + file.getCanonicalPath() + " created");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (args.get(0).equalsIgnoreCase("section")) {
            if (args.size() < 3) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Use /arena debug section <arena> [id]");
                return;
            }

            if (!Arena.arenas.containsKey(args.get(1).toLowerCase())) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena not found! Use /arena list");
                return;
            }

            int sectionID = 0;
            Arena arena = Arena.arenas.get(args.get(1).toLowerCase());

            if (args.size() > 2) {
                sectionID = Integer.parseInt(args.get(2));
            }

            List<String> lines = new ArrayList<>();

            lines.add(" ======= Section ID " + sectionID + " ======= ");
            lines.add("Parent Arena: " + arena.getName());
            lines.addAll(getSectionLines(arena, arena.getSections().get(sectionID)));

            byte[] bytes = String.join("\n", lines).getBytes(StandardCharsets.US_ASCII);
            File file = new File(PlatinumArenas.INSTANCE.getDataFolder(), "debug_section_" + sectionID + ".txt");
            try {
                Files.write(bytes, file);
                sender.sendMessage(ChatColor.GREEN + "File " + file.getCanonicalPath() + " created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args.get(0).equalsIgnoreCase("getarena")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You cannot use this command as a non player entity!");
                return;
            }
            Consumer<Block> consumer = (block) -> {
                List<Arena> arenas = Arena.arenas.values().stream().filter(a -> a.contains(block.getLocation())).collect(Collectors.toList());

                if (arenas.size() > 1) {
                    sender.sendMessage(ChatColor.GREEN + "Multiple arenas found for this block");
                } else if (arenas.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "No arenas found for this block");
                }
                arenas.forEach(a -> sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + a.getName()));
            };
            BLOCK_LISTENERS.put(((Player)sender).getUniqueId(), consumer);
            sender.sendMessage(ChatColor.GREEN + "Hit a block to see what arena is at this location");
        } else if (args.get(0).equalsIgnoreCase("getsection")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You cannot use this command as a non player entity!");
                return;
            }
            Consumer<Block> consumer = (block) -> {
                List<Section> sections = Arena.arenas.values().stream().filter(a -> a.contains(block.getLocation()))
                        .flatMap(arena -> arena.getSections().stream()).filter(s -> s.contains(block.getLocation()))
                        .collect(Collectors.toList());

                if (sections.size() > 1) {
                    sender.sendMessage(ChatColor.GREEN + "Multiple arenas found for this block");
                } else if (sections.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "No sections found for this block");
                }
                sections.forEach(s -> {
                    sender.sendMessage(ChatColor.GREEN + "Arena " + ChatColor.YELLOW + "" + ChatColor.BOLD + s.getParent().getName() + ChatColor.GREEN + " Section ID " + ChatColor.YELLOW + s.getID());
                });
            };
            BLOCK_LISTENERS.put(((Player)sender).getUniqueId(), consumer);
            sender.sendMessage(ChatColor.GREEN + "Hit a block to see what sections are at this location");
        }


    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
        ArrayList<String> completions = new ArrayList();

        if (args.size() <= 1) {
            completions.add("arena");
            completions.add("getarena");
            completions.add("section");
            completions.add("getsection");
        } else if (args.size() <= 2) {
            if (args.get(0).equalsIgnoreCase("arena") || args.get(0).equalsIgnoreCase("section")) {
                return Arena.arenas.values().stream().map(Arena::getName).collect(Collectors.toList());
            }
        } else if (args.size() <= 3) {
            if (args.get(0).equalsIgnoreCase("arena")) {
                return Arrays.asList("chat", "dump");
            } else if (args.get(0).equalsIgnoreCase("section")) {
                return Arena.arenas.entrySet().stream().filter( entry -> entry.getKey().equalsIgnoreCase(args.get(0)))
                        .flatMap(entry -> entry.getValue().getSections().stream()).map(s -> s.getID() + "").collect(Collectors.toList());
            }
        }
        return completions;
    }

    public List<String> getSectionLines(Arena arena, Section section) {
        List<String> lines = new ArrayList<>();
        lines.add("Start: " + section.getStart().toString());
        lines.add("End: " + section.getEnd().toString());
        lines.add("Width: " + section.getWidth());
        lines.add("Height: " + section.getWidth());
        lines.add("Length: " + section.getWidth());
        lines.add("Total blocks: " + section.getTotalBlocks());
        lines.add("NBT Blocks: " + section.getNBTCache().size());
        lines.add("= Blocks = ");
        for (int i = 0; i < section.getBlockTypes().length; i++) {
            short keyShort = section.getBlockTypes()[i];
            String key = arena.getKeys()[keyShort].getAsString(true);
            int amount = section.getBlockAmounts()[i];
            lines.add(key + " * " + amount);
        }
        lines.add(" = NBT Blocks = ");
        for (int index : section.getNBTCache().keySet()) {
            Pair<Wrapper, Object> nbt = section.getNBTCache().get(index);
            String clazz = nbt.getKey().getTileClass().toGenericString();
            String data = new String(nbt.getKey().write(nbt.getRight()), StandardCharsets.US_ASCII);

            lines.add(clazz + "=" + data);
        }
        return lines;
    }
}
