package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.CreateCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ArenaCommand {

    private static Map<String, ArenaCommand> subcommands = new HashMap<>();

    private String command = "arena";
    private String description = "Manage PlatinumArenas!";
    private String usage = "/arena help for a list of commands";

    public ArenaCommand(String command, String description, String usage, String[] aliases) {
        this.command = command;
        this.description = description;
        this.usage = usage;

        subcommands.put(command.toLowerCase(), this);
        for (String cmd : aliases) {
            subcommands.put(cmd.toLowerCase(), this);
        }
    }

    public abstract void execute(CommandSender sender, List<String> args);

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    /** Gets a list of valid arguments that can be used in tabbing. */
    protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
        return new ArrayList<String>();
    }

    private static void help(CommandSender sender) {

        List<String> list = new ArrayList<>();
        list.addAll(subcommands.keySet());
        list.sort(Comparator.naturalOrder());

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " List of subcommands:");

        for (String s : list) {
            sender.sendMessage(ChatColor.YELLOW + "/arena " + s + " - " + subcommands.get(s).description);
        }

    }

    static CommandExecutor getCommandExecutor() {
        final CommandExecutor exe = (sender, cmd, label, args) -> {
            if (args.length > 0) {
                if (subcommands.containsKey(args[0].toLowerCase())) {
                    subcommands.get(args[0].toLowerCase()).execute(sender, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                    return true;
                }
            }
            help(sender);
            return true;
        };
        return exe;
    }

    public static void createCommands() {
        subcommands.clear();
        subcommands.put("create", new CreateCommand());
    }
}
