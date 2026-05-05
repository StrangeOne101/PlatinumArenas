package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.ArenaIO;
import com.strangeone101.platinumarenas.ConfigManager;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.TimerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends ArenaCommand {

    public ReloadCommand() {
        super("reload", "Reload all arenas", "/arena reload", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.reload")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        ArenaIO.loadAllArenas();
        ConfigManager.setup();
        ResetCommand.ResetSpeed.reload();
        TimerManager.reload();
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Arenas, config and timers reloaded!");
    }
}
