package com.strangeone101.platinumarenas.commands;

import com.strangeone101.platinumarenas.Arena;
import com.strangeone101.platinumarenas.ArenaCommand;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.TimerManager;
import com.strangeone101.platinumarenas.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerCommand extends ArenaCommand {

    private static final Set<String> KEYWORDS = Set.of("every", "in", "at");
    private static final Set<String> MANAGEMENT = Set.of("list", "remove", "delete");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhdw])", Pattern.CASE_INSENSITIVE);
    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mma").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ha").toFormatter(Locale.ENGLISH),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H"),
            DateTimeFormatter.ofPattern("HH")
    );

    public TimerCommand() {
        super("timer", "Schedule arena commands", "/arena timer [arena] <every|in|at ...> <command>", new String[] {"schedule"});
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("platinumarenas.timer")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You don't have permission to run this command!");
            return;
        }

        if (args.isEmpty()) {
            sendUsage(sender);
            return;
        }

        String subcommand = args.get(0).toLowerCase(Locale.ROOT);
        if (subcommand.equals("list")) {
            listTimers(sender);
            return;
        }

        if (subcommand.equals("remove") || subcommand.equals("delete")) {
            removeTimer(sender, args);
            return;
        }

        if (!PlatinumArenas.INSTANCE.isReady()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arenas have not finished loading yet!");
            return;
        }

        ParsedTimer parsedTimer = parseTimer(sender, args);
        if (parsedTimer == null) {
            return;
        }

        TimerManager.ArenaTimer timer = TimerManager.addTimer(
                parsedTimer.scheduleText,
                String.join(" ", parsedTimer.commandTokens),
                parsedTimer.repeatMillis,
                parsedTimer.nextRunAt,
                sender.getName()
        );

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Added timer #" + timer.getId() + ChatColor.GREEN
                + " for " + ChatColor.YELLOW + parsedTimer.scheduleText + ChatColor.GREEN + ".");
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Command: " + ChatColor.YELLOW + "/arena "
                + String.join(" ", parsedTimer.commandTokens));
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Next run: " + ChatColor.YELLOW
                + TimerManager.formatTimestamp(parsedTimer.nextRunAt) + ChatColor.GRAY + " ("
                + TimerManager.formatRelativeUntil(parsedTimer.nextRunAt) + ChatColor.GRAY + ")");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Timer examples:");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer every 24h in 10m reset fast");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer in 2h reset normal");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer every 1d at 12pm reset fast");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer every 48h reset slow");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer <arena> every 24h reset fast");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer list");
        sender.sendMessage(ChatColor.YELLOW + " - /arena timer remove <id>");
    }

    private void listTimers(CommandSender sender) {
        Collection<TimerManager.ArenaTimer> timers = TimerManager.getTimers();
        if (timers.isEmpty()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " There are no timers scheduled.");
            return;
        }

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Scheduled timers:");
        for (TimerManager.ArenaTimer timer : timers) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " #" + timer.getId() + ChatColor.GRAY
                    + " [" + timer.getScheduleText() + "] " + ChatColor.YELLOW + "/arena " + timer.getCommand());
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GRAY + " Next run: "
                    + TimerManager.formatTimestamp(timer.getNextRunAt()) + " (" + TimerManager.formatRelativeUntil(timer.getNextRunAt())
                    + ")" + ChatColor.DARK_GRAY + " Created by " + timer.getCreatedBy());
        }
    }

    private void removeTimer(CommandSender sender, List<String> args) {
        if (args.size() < 2 || !Util.isInteger(args.get(1))) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Remove a timer with /arena timer remove <id>");
            return;
        }

        int id = Integer.parseInt(args.get(1));
        TimerManager.ArenaTimer timer = TimerManager.removeTimer(id);
        if (timer == null) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timer #" + id + " was not found.");
            return;
        }

        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Removed timer #" + id + ".");
    }

    private ParsedTimer parseTimer(CommandSender sender, List<String> args) {
        int index = 0;
        String boundArena = null;

        if (!isKeyword(args.get(index)) && !MANAGEMENT.contains(args.get(index).toLowerCase(Locale.ROOT))) {
            boundArena = args.get(index).toLowerCase(Locale.ROOT);
            if (!Arena.arenas.containsKey(boundArena)) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Arena by that name not found!");
                return null;
            }
            index++;
        }

        if (index >= args.size()) {
            sendUsage(sender);
            return null;
        }

        int scheduleStart = index;
        Long repeatMillis = null;
        Long delayMillis = null;
        LocalTime atTime = null;

        while (index < args.size() && isKeyword(args.get(index))) {
            String keyword = args.get(index).toLowerCase(Locale.ROOT);
            if (index + 1 >= args.size()) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Missing value after \"" + keyword + "\".");
                return null;
            }

            String value = args.get(index + 1);
            switch (keyword) {
                case "every":
                    if (repeatMillis != null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You can only use \"every\" once.");
                        return null;
                    }
                    repeatMillis = parseDuration(value);
                    if (repeatMillis == null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Invalid duration \"" + value + "\".");
                        return null;
                    }
                    break;
                case "in":
                    if (delayMillis != null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You can only use \"in\" once.");
                        return null;
                    }
                    delayMillis = parseDuration(value);
                    if (delayMillis == null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Invalid duration \"" + value + "\".");
                        return null;
                    }
                    break;
                case "at":
                    if (atTime != null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " You can only use \"at\" once.");
                        return null;
                    }
                    atTime = parseTime(value);
                    if (atTime == null) {
                        sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Invalid time \"" + value + "\".");
                        return null;
                    }
                    break;
                default:
                    break;
            }

            index += 2;
        }

        if (index == scheduleStart) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timers must include at least one of: every, in, at.");
            return null;
        }

        if (delayMillis != null && atTime != null) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Use either \"in\" or \"at\", not both.");
            return null;
        }

        if (index >= args.size()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timers need a command to execute.");
            return null;
        }

        List<String> commandTokens = resolveCommandTokens(sender, boundArena, new ArrayList<>(args.subList(index, args.size())));
        if (commandTokens == null) {
            return null;
        }

        long nextRunAt = calculateNextRun(repeatMillis, delayMillis, atTime);
        String scheduleText = String.join(" ", args.subList(scheduleStart, index));

        return new ParsedTimer(scheduleText, commandTokens, repeatMillis, nextRunAt);
    }

    private List<String> resolveCommandTokens(CommandSender sender, String boundArena, List<String> commandTokens) {
        if (commandTokens.isEmpty()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timers need a command to execute.");
            return null;
        }

        List<String> normalized = new ArrayList<>(commandTokens);
        normalized.set(0, normalized.get(0).startsWith("/") ? normalized.get(0).substring(1) : normalized.get(0));
        if (!normalized.isEmpty() && isCommandRoot(normalized.get(0))) {
            normalized.remove(0);
        }

        if (normalized.isEmpty()) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timers need an arena subcommand to run.");
            return null;
        }

        String subcommand = normalized.get(0).toLowerCase(Locale.ROOT);
        if (subcommand.equals("timer")) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Timers cannot schedule other timer commands.");
            return null;
        }

        if (!ArenaCommand.hasSubcommand(subcommand)) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Unknown arena command \"" + normalized.get(0) + "\".");
            return null;
        }

        if (subcommand.equals("reset") && (normalized.size() == 1 || !Arena.arenas.containsKey(normalized.get(1).toLowerCase(Locale.ROOT)))) {
            String arena = boundArena != null ? boundArena : inferArena(sender);
            if (arena == null) {
                sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Reset timers need an arena. Use /arena timer <arena> ... or run the command while standing inside a single arena.");
                return null;
            }

            normalized.add(1, arena);
        }

        if (subcommand.equals("reset") && normalized.size() >= 3
                && ResetCommand.ResetSpeed.getSpeed(normalized.get(2)) == ResetCommand.ResetSpeed.INSTANT) {
            sender.sendMessage(PlatinumArenas.PREFIX + ChatColor.RED + " Instant resets cannot be scheduled because they require manual confirmation.");
            return null;
        }

        return normalized;
    }

    private String inferArena(CommandSender sender) {
        if (Arena.arenas.size() == 1) {
            return Arena.arenas.values().iterator().next().getName();
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;
        List<Arena> matches = Arena.arenas.values().stream()
                .filter(arena -> arena.contains(player.getLocation()))
                .sorted(Comparator.comparingInt(Arena::getTotalBlocks))
                .toList();

        if (matches.size() == 1) {
            return matches.get(0).getName();
        }

        return null;
    }

    private boolean isCommandRoot(String string) {
        return string.equalsIgnoreCase("arena")
                || string.equalsIgnoreCase("arenas")
                || string.equalsIgnoreCase("platinumarenas")
                || string.equalsIgnoreCase("pta");
    }

    private boolean isKeyword(String string) {
        return KEYWORDS.contains(string.toLowerCase(Locale.ROOT));
    }

    private Long parseDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input);
        long totalMillis = 0L;
        int index = 0;

        while (matcher.find()) {
            if (matcher.start() != index) {
                return null;
            }

            long amount = Long.parseLong(matcher.group(1));
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));

            switch (unit) {
                case 's':
                    totalMillis += Duration.ofSeconds(amount).toMillis();
                    break;
                case 'm':
                    totalMillis += Duration.ofMinutes(amount).toMillis();
                    break;
                case 'h':
                    totalMillis += Duration.ofHours(amount).toMillis();
                    break;
                case 'd':
                    totalMillis += Duration.ofDays(amount).toMillis();
                    break;
                case 'w':
                    totalMillis += Duration.ofDays(amount * 7L).toMillis();
                    break;
                default:
                    return null;
            }

            index = matcher.end();
        }

        if (index != input.length() || totalMillis <= 0L) {
            return null;
        }

        return totalMillis;
    }

    private LocalTime parseTime(String input) {
        String normalized = input.toUpperCase(Locale.ROOT);
        for (DateTimeFormatter formatter : TIME_FORMATS) {
            try {
                return LocalTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {}
        }

        return null;
    }

    private long calculateNextRun(Long repeatMillis, Long delayMillis, LocalTime atTime) {
        if (delayMillis != null) {
            return System.currentTimeMillis() + delayMillis;
        }

        ZonedDateTime now = ZonedDateTime.now();
        if (atTime != null) {
            ZonedDateTime next = now.withHour(atTime.getHour()).withMinute(atTime.getMinute()).withSecond(0).withNano(0);
            long stepMillis = repeatMillis != null ? repeatMillis : Duration.ofDays(1).toMillis();

            while (!next.isAfter(now)) {
                next = next.plus(Duration.ofMillis(stepMillis));
            }

            return next.toInstant().toEpochMilli();
        }

        if (repeatMillis != null) {
            return System.currentTimeMillis() + repeatMillis;
        }

        throw new IllegalStateException("Timer parsing accepted a schedule with no execution point");
    }

    @Override
    protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
        List<String> completions = new ArrayList<>();

        if (args.isEmpty()) {
            return completions;
        }

        if (args.size() == 1) {
            completions.addAll(Arrays.asList("list", "remove", "every", "in", "at"));
            completions.addAll(Arena.arenas.keySet());
            completions.sort(Comparator.naturalOrder());
            return completions;
        }

        if (args.get(0).equalsIgnoreCase("remove") || args.get(0).equalsIgnoreCase("delete")) {
            if (args.size() == 2) {
                for (TimerManager.ArenaTimer timer : TimerManager.getTimers()) {
                    completions.add(String.valueOf(timer.getId()));
                }
            }
            completions.sort(Comparator.naturalOrder());
            return completions;
        }

        int scheduleIndex = 0;
        if (!isKeyword(args.get(0)) && Arena.arenas.containsKey(args.get(0).toLowerCase(Locale.ROOT))) {
            scheduleIndex = 1;
        }

        if (args.size() > scheduleIndex) {
            int current = args.size() - 1;
            if (current == scheduleIndex) {
                completions.addAll(Arrays.asList("every", "in", "at"));
                return completions;
            }

            if (current > scheduleIndex && isKeyword(args.get(current - 1))) {
                String previous = args.get(current - 1).toLowerCase(Locale.ROOT);
                if (previous.equals("at")) {
                    completions.addAll(Arrays.asList("12pm", "6pm", "00:00", "12:00"));
                } else {
                    completions.addAll(Arrays.asList("10m", "1h", "2h", "1d", "24h", "48h"));
                }
                return completions;
            }
        }

        int index = scheduleIndex;
        while (index < args.size()) {
            String token = args.get(index).toLowerCase(Locale.ROOT);
            if (!isKeyword(token)) {
                break;
            }

            index += 2;
        }

        int commandOffset = args.size() - index;
        if (commandOffset <= 1) {
            completions.addAll(ArenaCommand.getSubcommandNames().stream()
                    .filter(name -> !name.equalsIgnoreCase("timer"))
                    .toList());
            return completions;
        }

        String command = args.get(index).toLowerCase(Locale.ROOT);
        if (command.equals("reset")) {
            if (commandOffset == 2) {
                completions.addAll(Arena.arenas.keySet());
                completions.addAll(Arrays.asList("veryslow", "slow", "normal", "fast", "veryfast", "extreme", "instant"));
            } else if (commandOffset == 3) {
                completions.addAll(Arrays.asList("veryslow", "slow", "normal", "fast", "veryfast", "extreme", "instant", "-silent"));
            } else if (commandOffset == 4) {
                completions.add("-silent");
            }
        }

        completions.sort(Comparator.naturalOrder());
        return completions;
    }

    private static class ParsedTimer {
        private final String scheduleText;
        private final List<String> commandTokens;
        private final Long repeatMillis;
        private final long nextRunAt;

        private ParsedTimer(String scheduleText, List<String> commandTokens, Long repeatMillis, long nextRunAt) {
            this.scheduleText = scheduleText;
            this.commandTokens = commandTokens;
            this.repeatMillis = repeatMillis;
            this.nextRunAt = nextRunAt;
        }
    }
}
