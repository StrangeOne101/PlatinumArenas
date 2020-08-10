package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.ConfigManager;
import com.strangeone101.platinumarenas.PlatinumArenas;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ResetCommand extends ArenaCommand {

    public ResetCommand() {
        super("reset", "Reset an arena", "/arena reset <arena> [speed]", new String[] {"revert"});
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.reset")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        if (args.size() == 0) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Reset an arena by running /arena reset <arena> [speed]");
            return;
        }

        if (!Arena.arenas.containsKey(args.get(0).toLowerCase())) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena by that name not found!");
            return;
        }

        if (Arena.arenas.get(args.get(0).toLowerCase()).isBeingReset()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " That arena is currently being reset!");
            return;
        }

        ResetSpeed speed = ResetSpeed.NORMAL;
        if (args.size() >= 2) {
            speed = ResetSpeed.getSpeed(args.get(1));
        }

        Arena arena = Arena.arenas.get(args.get(0).toLowerCase());

        if (speed == ResetSpeed.INSTANT) { //If they want to reset the arena instantly, warn them beforehand.
            if (!sender.hasPermission("platinumarenas.reset.instant")) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to reset arenas instantly!");
                return;
            }
            ResetSpeed finalSpeed = speed;
            ConfirmCommand.confirmTasks.put(sender, () -> resetArena(arena, finalSpeed, sender));
            new BukkitRunnable() {
                @Override
                public void run() {
                    ConfirmCommand.confirmTasks.remove(sender);
                }
            }.runTaskLater(PlatinumArenas.INSTANCE, 20 * 60); //Remove the task after a min
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Are you sure you want to reset arena \"" + arena.getName() + "\" instantly?");
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " To confirm this, use " + ChatColor.RED + "/arena confirm");
        } else {
            resetArena(arena, speed, sender);
        }

    }

    private void resetArena(Arena arena, ResetSpeed speed, CommandSender sender) {
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Resetting arena \"" + arena.getName() + "\"!");
        long time = System.currentTimeMillis();
        arena.reset(speed.getAmount() / 20, () -> {
            if (sender != null && (!(sender instanceof Player) || ((Player)sender).isOnline())) {
                long took = System.currentTimeMillis() - time;
                String tookS = took < 1000 ? took + "ms" : (took > 1000 * 120 ? took / 60000 + "m" : took / 1000 + "s");
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Arena \"" + arena.getName() + "\" reset complete (took " + tookS + ")!");
            }

        });
    }


    public enum ResetSpeed {

        VERY_SLOW(ConfigManager.BLOCKS_RESET_PER_SECOND_VERYSLOW, "veryslow"),
        SLOW(ConfigManager.BLOCKS_RESET_PER_SECOND_SLOW),
        NORMAL(ConfigManager.BLOCKS_RESET_PER_SECOND_NORMAL),
        FAST(ConfigManager.BLOCKS_RESET_PER_SECOND_FAST),
        VERY_FAST(ConfigManager.BLOCKS_RESET_PER_SECOND_VERYFAST, "veryfast"),
        EXTREMELY_FAST(ConfigManager.BLOCKS_RESET_PER_SECOND_EXTREME, "extremelyfast", "extreme"),
        INSTANT(Integer.MAX_VALUE, "instantly");

        private int amount;
        private String[] alias;

        ResetSpeed(int amount, String... alias) {
            this.amount = amount;
            this.alias = alias;
        }

        public int getAmount() {
            return amount;
        }

        public static ResetSpeed getSpeed(String string) {
            for (ResetSpeed speed : values()) {
                if (string.equalsIgnoreCase(speed.name()) || Arrays.asList(speed.alias).contains(string.toLowerCase())) {
                    return speed;
                }
            }
            return NORMAL;
        }
    }

    @Override
    protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
        List<String> completions = new ArrayList<>();
        if (args.size() == 0) {
            completions.addAll(Arena.arenas.keySet());
            completions.sort(Comparator.naturalOrder());
        } else if (args.size() == 1) {
            completions.addAll(Arrays.asList(new String[] {"veryslow", "slow", "normal", "fast", "veryfast", "extreme"}));
        }

        return super.getTabCompletion(sender, args);
    }
}
