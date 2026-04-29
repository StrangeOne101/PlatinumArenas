package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.BorderCommand;
import com.strangeone101.platinumarenas.commands.CancelCommand;
import com.strangeone101.platinumarenas.commands.ConfirmCommand;
import com.strangeone101.platinumarenas.commands.CreateCommand;
import com.strangeone101.platinumarenas.commands.DebugCommand;
import com.strangeone101.platinumarenas.commands.DeleteCommand;
import com.strangeone101.platinumarenas.commands.InfoCommand;
import com.strangeone101.platinumarenas.commands.ListCommand;
import com.strangeone101.platinumarenas.commands.ReloadCommand;
import com.strangeone101.platinumarenas.commands.ResetCommand;
import com.strangeone101.platinumarenas.commands.TimerCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.aliases = aliases;

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

    private static ArenaCommand findSubcommand(String string) {
        for (String s : subcommands.keySet()) {
            ArenaCommand subcommand = subcommands.get(s);
            if (string.equalsIgnoreCase(s) || Arrays.asList(subcommand.getAliases()).contains(string.toLowerCase())) {
                return subcommand;
            }
        }

        return null;
    }

    public static boolean hasSubcommand(String string) {
        return findSubcommand(string) != null;
    }

    public static List<String> getSubcommandNames() {
        LinkedHashSet<String> commands = new LinkedHashSet<>();
        for (ArenaCommand command : subcommands.values()) {
            commands.add(command.getCommand());
        }

        return new ArrayList<>(commands);
    }

    public static boolean dispatch(CommandSender sender, List<String> args) {
        if (args.size() > 0) {
            ArenaCommand subcommand = findSubcommand(args.get(0));
            if (subcommand != null) {
                subcommand.execute(sender, new ArrayList<>(args.subList(1, args.size())));
                return true;
            }
        }

        help(sender);
        return false;
    }

    static CommandExecutor getCommandExecutor() {
        final CommandExecutor exe = (sender, cmd, label, args) ->
                dispatch(sender, new ArrayList<>(Arrays.asList(args)));
        return exe;
    }

    static TabCompleter getTabCompleter() {
        final TabCompleter completer = (sender, cmd, label, args) -> {
            if (args.length > 1) {
                ArenaCommand subcommand = findSubcommand(args[0]);
                if (subcommand != null) {
                    List<String> listArgs = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                    return subcommand.getTabCompletion(sender, listArgs).stream()
                            .filter(s -> s.startsWith(listArgs.get(listArgs.size() - 1))).collect(Collectors.toList());
                }
            } else {
                return subcommands.values().stream().filter(
                        (subcmd) -> !subcmd.isHidden() && sender.hasPermission("platinumarenas." + subcmd.getCommand()))
                        .map((subcmd) -> subcmd.getCommand()).filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
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
        subcommands.put("info", new InfoCommand());
        subcommands.put("reload", new ReloadCommand());
        subcommands.put("debug", new DebugCommand());
        subcommands.put("timer", new TimerCommand());
    }
}
