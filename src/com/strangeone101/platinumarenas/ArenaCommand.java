package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.BorderCommand;
import com.strangeone101.platinumarenas.commands.CancelCommand;
import com.strangeone101.platinumarenas.commands.ConfirmCommand;
import com.strangeone101.platinumarenas.commands.CreateCommand;
import com.strangeone101.platinumarenas.commands.DebugCommand;
import com.strangeone101.platinumarenas.commands.DeleteCommand;
import com.strangeone101.platinumarenas.commands.ListCommand;
import com.strangeone101.platinumarenas.commands.ResetCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

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
    private String[] aliases = new String[0];

    public ArenaCommand(String command, String description, String usage, String[] aliases) {
        this.command = command;
        this.description = description;
        this.usage = usage;

        subcommands.put(command.toLowerCase(), this);
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

    public String[] getAliases() {
        return aliases;
    }

    public boolean isHidden() {
        return false;
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
            if (subcommands.get(s).isHidden()) continue;

            sender.sendMessage(ChatColor.YELLOW + "/arena " + s + " - " + subcommands.get(s).description);
        }

    }

    static CommandExecutor getCommandExecutor() {
        final CommandExecutor exe = (sender, cmd, label, args) -> {
            if (args.length > 0) {
                for (String s : subcommands.keySet()) {
                    if (args[0].equalsIgnoreCase(s) || Arrays.asList(subcommands.get(s).getAliases()).contains(args[0].toLowerCase())) {
                        subcommands.get(s).execute(sender, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                        return true;
                    }
                }
            }
            help(sender);
            return true;
        };
        return exe;
    }

    static TabCompleter getTabCompleter() {
        final TabCompleter completer = (sender, cmd, label, args) -> {
            if (args.length > 0) {
                if (subcommands.containsKey(args[0].toLowerCase())) {
                    return subcommands.get(args[0].toLowerCase()).getTabCompletion(sender, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                }
            }
            return new ArrayList<>();
        };
        return completer;
    }

    public static void createCommands() {
        subcommands.clear();
        subcommands.put("create", new CreateCommand());
        subcommands.put("reset", new ResetCommand());
        subcommands.put("list", new ListCommand());
        subcommands.put("border", new BorderCommand());
        subcommands.put("remove", new DeleteCommand());
        subcommands.put("confirm", new ConfirmCommand());
        subcommands.put("cancel", new CancelCommand());
        subcommands.put("debug", new DebugCommand());
    }
}
