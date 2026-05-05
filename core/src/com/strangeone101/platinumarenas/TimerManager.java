package com.strangeone101.platinumarenas;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TimerManager {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());

    private static final Map<Integer, ArenaTimer> TIMERS = new LinkedHashMap<>();

    private static BukkitRunnable loop;
    private static File file;
    private static int nextId = 1;

    public static void reload() {
        file = new File(PlatinumArenas.INSTANCE.getDataFolder(), "timers.yml");
        load();
        startLoop();
    }

    public static void shutdown() {
        if (loop != null) {
            loop.cancel();
            loop = null;
        }

        save();
        TIMERS.clear();
        nextId = 1;
    }

    public static ArenaTimer addTimer(String scheduleText, String command, Long repeatMillis, long nextRunAt, String createdBy) {
        ArenaTimer timer = new ArenaTimer(nextId++, scheduleText, command, repeatMillis, nextRunAt, createdBy);
        TIMERS.put(timer.getId(), timer);
        save();
        return timer;
    }

    public static ArenaTimer removeTimer(int id) {
        ArenaTimer timer = TIMERS.remove(id);
        if (timer != null) {
            save();
        }

        return timer;
    }

    public static Collection<ArenaTimer> getTimers() {
        return new ArrayList<>(TIMERS.values());
    }

    public static String formatTimestamp(long time) {
        return DATE_FORMAT.format(Instant.ofEpochMilli(time));
    }

    public static String formatRelativeUntil(long time) {
        long difference = time - System.currentTimeMillis();
        if (difference <= 0) {
            return "now";
        }

        return "in " + formatDuration(difference);
    }

    public static String formatDuration(long millis) {
        long totalSeconds = Math.max(1L, millis / 1000L);
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        List<String> parts = new ArrayList<>();
        if (days > 0) parts.add(days + "d");
        if (hours > 0) parts.add(hours + "h");
        if (minutes > 0) parts.add(minutes + "m");
        if (seconds > 0 && parts.isEmpty()) parts.add(seconds + "s");

        return parts.stream().limit(2).collect(Collectors.joining(" "));
    }

    private static void startLoop() {
        if (loop != null) {
            return;
        }

        loop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!PlatinumArenas.INSTANCE.isReady()) {
                    return;
                }

                tick();
            }
        };
        loop.runTaskTimer(PlatinumArenas.INSTANCE, 20L, 20L);
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        boolean save = false;

        for (ArenaTimer timer : new ArrayList<>(TIMERS.values())) {
            if (timer.getNextRunAt() > now) {
                continue;
            }

            PlatinumArenas.INSTANCE.getLogger().info("Executing timer #" + timer.getId() + ": /arena " + timer.getCommand());
            ArenaCommand.dispatch(Bukkit.getConsoleSender(), tokenize(timer.getCommand()));

            if (timer.getRepeatMillis() != null) {
                timer.advancePast(now);
            } else {
                TIMERS.remove(timer.getId());
            }

            save = true;
        }

        if (save) {
            save();
        }
    }

    private static List<String> tokenize(String command) {
        return Arrays.stream(command.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static void load() {
        TIMERS.clear();
        nextId = 1;

        if (!file.exists()) {
            save();
            return;
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            PlatinumArenas.INSTANCE.getLogger().severe("Failed to load timers.yml!");
            e.printStackTrace();
            return;
        }

        nextId = Math.max(1, configuration.getInt("next-id", 1));

        ConfigurationSection section = configuration.getConfigurationSection("timers");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            if (!Util.isInteger(key)) {
                continue;
            }

            ConfigurationSection timerSection = section.getConfigurationSection(key);
            if (timerSection == null) {
                continue;
            }

            String schedule = timerSection.getString("schedule", "");
            String command = timerSection.getString("command", "");
            long nextRunAt = timerSection.getLong("next-run-at", 0L);

            if (command.isBlank() || nextRunAt <= 0L) {
                continue;
            }

            int id = Integer.parseInt(key);
            Long repeatMillis = timerSection.contains("repeat-ms") ? timerSection.getLong("repeat-ms") : null;
            String createdBy = timerSection.getString("created-by", "Unknown");

            TIMERS.put(id, new ArenaTimer(id, schedule, command, repeatMillis, nextRunAt, createdBy));
            if (id >= nextId) {
                nextId = id + 1;
            }
        }
    }

    private static void save() {
        if (file == null) {
            return;
        }

        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("next-id", nextId);

        for (ArenaTimer timer : TIMERS.values()) {
            String path = "timers." + timer.getId();
            configuration.set(path + ".schedule", timer.getScheduleText());
            configuration.set(path + ".command", timer.getCommand());
            configuration.set(path + ".next-run-at", timer.getNextRunAt());
            configuration.set(path + ".repeat-ms", timer.getRepeatMillis());
            configuration.set(path + ".created-by", timer.getCreatedBy());
        }

        try {
            configuration.save(file);
        } catch (IOException e) {
            PlatinumArenas.INSTANCE.getLogger().severe("Failed to save timers.yml!");
            e.printStackTrace();
        }
    }

    public static class ArenaTimer {
        private final int id;
        private final String scheduleText;
        private final String command;
        private final Long repeatMillis;
        private final String createdBy;
        private long nextRunAt;

        private ArenaTimer(int id, String scheduleText, String command, Long repeatMillis, long nextRunAt, String createdBy) {
            this.id = id;
            this.scheduleText = scheduleText;
            this.command = command;
            this.repeatMillis = repeatMillis;
            this.nextRunAt = nextRunAt;
            this.createdBy = createdBy;
        }

        public int getId() {
            return id;
        }

        public String getScheduleText() {
            return scheduleText;
        }

        public String getCommand() {
            return command;
        }

        public Long getRepeatMillis() {
            return repeatMillis;
        }

        public long getNextRunAt() {
            return nextRunAt;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        private void advancePast(long now) {
            if (repeatMillis == null) {
                return;
            }

            do {
                nextRunAt += repeatMillis;
            } while (nextRunAt <= now);
        }
    }
}
