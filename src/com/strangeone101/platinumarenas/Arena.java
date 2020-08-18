package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.DebugCommand;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    public static Map<String, Arena> arenas = new HashMap<>();

    private String name;
    private UUID creator;

    private BlockData[] keys;

    private Location corner1;
    private Location corner2;

    private boolean beingReset = false;
    private boolean cancelReset = false;

    @Deprecated
    private short[] blocks;

    private List<Section> sections = new ArrayList<>();

    private Map<String, ArenaProperty> properties = new HashMap<>();

    Arena(String name, Location corner1, Location corner2) { //package level access
        this.name = name;
        this.corner1 = corner1;
        this.corner2 = corner2;

        if (corner1.getWorld() != corner2.getWorld()) {
            return;
        }

        if (!name.matches("[\\w\\d]{3,}")) {
            return;
        }

        keys = new BlockData[] {
            Material.AIR.createBlockData(),
            Material.DIRT.createBlockData(),
            Material.GRASS_BLOCK.createBlockData(),
        };

        int x1 = corner1.getBlockX();
        int x2 = corner2.getBlockX();
        int y1 = corner1.getBlockY();
        int y2 = corner2.getBlockY();
        int z1 = corner1.getBlockZ();
        int z2 = corner2.getBlockZ();

        if (x1 > x2) { //Flip variables to make sure x1 is smaller
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }

        if (y1 > y2) { //Flip variables to make sure y1 is smaller
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }

        if (z1 > z2) { //Flip variables to make sure z1 is smaller
            int temp = z2;
            z2 = z1;
            z1 = temp;
        }

        corner1.setX(x1);
        corner2.setX(x2);
        corner1.setY(y1);
        corner2.setY(y2);
        corner1.setZ(z1);
        corner2.setZ(z2);
    }

    /**
     * Merge two arenas together. This happens automatically when smaller temporary
     * arenas are created and then merged back into one larger arena in order to
     * prevent a lot of lag.
     *
     * @param keys The keyset
     * @param blocks The blocks
     */
    @Deprecated
    void merge(BlockData[] keys, short[] blocks) {
        List<BlockData> keyList = new ArrayList<>(Arrays.asList(this.keys));
        Map<Short, Short> corrections = new HashMap<Short, Short>();

        //Loop through all the keys in the new keyset. Add them to the full keyset if they aren't in it already
        for (int i = 0; i < keys.length; i++) {
            if (!keyList.contains(keys[i])) {
                keyList.add(keys[i]);
                this.keys = (BlockData[]) keyList.toArray(); //Update the keys to feature this blockdata
            }

            //We are now storing the old block index of this key as well as the new one.
            short newKeyIndex = (short) ArrayUtils.indexOf(this.keys, keys[i]);
            corrections.put((short) i, newKeyIndex); //Store the old int value and the new one that it should change to
        }

        //Loop through all blocks and change their block index from one from the old keyset into the one from the new keyset
        for (int b = 0; b < blocks.length; b++) {
            blocks[b] = corrections.get(blocks[b]); //Get the old block index and set it to the one that exists in this keyset
        }

        this.blocks = ArrayUtils.addAll(this.blocks, blocks);
    }

    public void addKeys(Collection<BlockData> keys) {
        long time = System.currentTimeMillis();
        List<BlockData> keyList = new ArrayList<>(Arrays.asList(this.keys));
        for (BlockData data : keys) {
            if (!keyList.contains(data)) keyList.add(data);
        }

        this.keys = keyList.toArray(new BlockData[keyList.size()]);
        //PlatinumArenas.INSTANCE.getLogger().info("Key took " + (System.currentTimeMillis() - time) + "ms");
    }

    public void reset(int resetSpeed, CommandSender sender, Runnable... callback) {
        if (getSections().size() == 0) return;

        this.beingReset = true;

        ResetLoopinData data = new ResetLoopinData();
        data.maxBlocksThisTick = resetSpeed;
        data.speed = resetSpeed;
        data.sender = sender;
        for (Section s : getSections()) {
            int sectionAmount = (int)((double)resetSpeed / (double)getTotalBlocks() * (double)s.getTotalBlocks());
            if (sectionAmount <= 0) sectionAmount = 1; //Do AT LEAST one block per tick in each section
            data.sections.put(s.getID(), sectionAmount); //Store the amount of blocks each section should reset per tick
        }

        loopyReset(data, callback);
    }

    /**
     * Reset with recursion until complete, with each layer adding delay to the last
     * @param data The reset data, including sections and the amounts they reset, etc
     */
    private void loopyReset(ResetLoopinData data, Runnable... callbacks) {
        if (cancelReset) {
            beingReset = false;
            cancelReset = false;

            for (Section section : sections) {
                section.cancelReset();
            }
            return;
        }
        data.blocksThisTick = 0;
        int sectionsRemovedThisTick = 0;

        for (int sectionsIterated = 0; sectionsIterated < data.sections.size(); sectionsIterated++) {
            int id = (data.sections.keySet().toArray(new Integer[data.sections.size()])[(sectionsIterated + data.currentSectionResetting) % data.sections.size()]) % getSections().size(); //Get number x in list + offset, and wrap around with %
            Section s = getSections().get(id);
            long t = System.nanoTime();
            boolean reset = s.reset(data.sections.get(id));
            data.resetMicroseconds += System.nanoTime() - t;
            if (reset) {
                t = System.nanoTime();
                data.sections.remove(id);
                sectionsIterated--;
                sectionsRemovedThisTick++;

                if (data.sections.size() == 0) {
                    data.calculateMicroseconds += System.nanoTime() - t;
                    break;
                }

                int newTotalAmount = data.sections.keySet().parallelStream().mapToInt((sectionid) -> (getSections().get(sectionid).getTotalBlocks())).sum();

                //Recalculate how many blocks to do each tick
                List<Section> sectionList = data.sections.keySet().parallelStream().map((sectionid) -> getSections().get(sectionid)).collect(Collectors.toList());
                for (Section s1 : sectionList) {
                    int sectionAmount = (int) ((double)data.speed / (double)newTotalAmount * (double)s.getTotalBlocks());
                    if (sectionAmount <= 0) sectionAmount = 1; //Do AT LEAST one block per tick in each section
                    data.sections.put(s1.getID(), sectionAmount); //Store the amount of blocks each section should reset per tick
                }
                data.calculateMicroseconds += System.nanoTime() - t;
            }
            data.blocksThisTick += s.getBlocksResetThisTick();

            //If we have gone over the max, set the section we should start at next tick
            if (data.blocksThisTick > data.maxBlocksThisTick) {
                data.currentSectionResetting = (sectionsIterated + data.currentSectionResetting) % data.sections.size();
                data.blocksThisTick += s.getBlocksResetThisTick();
                break;
            }

        }

        if (data.sections.size() == 0) {
            for (Runnable r : callbacks) r.run();
            beingReset = false;

            String s = "Reset took " + (data.resetMicroseconds / 1000000) + "ms" + "\n" + "Reset calculations took " + (data.calculateMicroseconds / 1000000) + "ms";
            DebugCommand.debugString = s;

            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                loopyReset(data, callbacks);
            }
        }.runTaskLater(PlatinumArenas.INSTANCE, 1L);
    }

    /**
     * Create a new arena
     * @param name Arena name
     * @param corner1 One corner of the region
     * @param corner2 Other corner of the region
     * @param player The creator
     * @return The arena object
     */
    public static Arena createNewArena(String name, Location corner1, Location corner2, Player player) {

        int x1 = corner1.getBlockX();
        int x2 = corner2.getBlockX();
        int y1 = corner1.getBlockY();
        int y2 = corner2.getBlockY();
        int z1 = corner1.getBlockZ();
        int z2 = corner2.getBlockZ();

        if (x1 > x2) { //Flip variables to make sure x1 is smaller
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }

        if (y1 > y2) { //Flip variables to make sure y1 is smaller
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }

        if (z1 > z2) { //Flip variables to make sure z1 is smaller
            int temp = z2;
            z2 = z1;
            z1 = temp;
        }

        corner1.setX(x1);
        corner2.setX(x2);
        corner1.setY(y1);
        corner2.setY(y2);
        corner1.setZ(z1);
        corner2.setZ(z2); //Now we KNOW that corner1 is the smallest and corner2 is the largest on the axis

        player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Creating a new arena named \"" + name + "\"...");

        int width = corner2.getBlockX() - corner1.getBlockX() + 1;
        int length = corner2.getBlockZ() - corner1.getBlockZ() + 1;
        int height = corner2.getBlockY() - corner1.getBlockY() + 1;

        int maxSectionArea = ConfigManager.BLOCKS_PER_SECTION / height;
        int sectionArea = width * length;
        int sectionsX = 1;
        int sectionsZ = 1;
        boolean x = true;

        while (sectionArea > maxSectionArea) {
            if (x) sectionsX++;
            else sectionsZ++;

            sectionArea = (width / sectionsX) * (length / sectionsZ);

            x = !x; //flip
        }

        List<Location> sectionStarts = new ArrayList<>();
        List<Location> sectionEnds = new ArrayList<>();

        for (int sx = 0; sx < sectionsX; sx++) {
            for (int zx = 0; zx < sectionsZ; zx++) {

                int xStart = (int)(Math.floor(width / sectionsX) * sx);
                int zStart = (int)(Math.floor(length / sectionsZ) * zx);

                int xEnd = (int)(Math.floor(width / sectionsX) * (sx + 1)) - 1;
                int zEnd = (int)(Math.floor(length / sectionsZ) * (zx + 1)) - 1;

                if (sx == sectionsX - 1) xEnd = width - 1;
                if (zx == sectionsZ - 1) zEnd = length - 1;

                Location start = corner1.clone().add(xStart, 0, zStart);
                Location end = corner1.clone().add(xEnd, height - 1, zEnd);

                sectionStarts.add(start);
                sectionEnds.add(end);

                //PlatinumArenas.INSTANCE.getLogger().info("Debug: " + start.toString() + ", " + end.toString());

            }
        }

        String totalSize = NumberFormat.getInstance().format(width * length * height);
        PlatinumArenas.INSTANCE.getLogger().info("Creating new arena. Size is " + width + " x " + height + " x " + length + " and totals " + totalSize + " blocks.");

        player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Arena size is " + ChatColor.YELLOW + width + " x " + height + " x " + length + " " + ChatColor.GREEN + " and totals " + ChatColor.YELLOW + totalSize + " blocks.");
        if (sectionsX * sectionsZ > 1) player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " " + (sectionsX * sectionsZ) + " sections will be created.");
        player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Now analyzing individual blocks... (This may take a while)");

        //totalling " + totalSize + " blocks has been split into " + (sectionsX * sectionsZ) + " sections.");

        Arena arena = new Arena(name, corner1, corner2);

        CreationLoopinData data = new CreationLoopinData();
        data.arena = arena;
        data.sectionStarts = sectionStarts;
        data.sectionEnds = sectionEnds;
        data.sections = new ArrayList<>();
        data.maxBlocks = width * length * height;
        data.tick = 1;
        data.lastUpdate = System.currentTimeMillis() - 5000;

        long time = System.currentTimeMillis();

        Runnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                long took = System.currentTimeMillis() - time;

                arena.getSections().addAll(data.sections);

                String tookS = took < 1000 ? took + "ms" : (took > 1000 * 120 ? took / 60000 + "m" : took / 1000 + "s");
                if (player != null && player.isOnline()) {
                    player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Done analying! Took " + tookS + " over " + data.tick + " ticks.");
                    player.sendMessage(PlatinumArenas.PREFIX + ChatColor.YELLOW + " Saving to disk...");
                }

                Arena.arenas.put(arena.name, arena);
                ArenaIO.saveArena(new File(PlatinumArenas.INSTANCE.getDataFolder(), "/Arenas/" + name + ".dat"), arena);

                if (player != null && player.isOnline()) {
                    player.sendMessage(PlatinumArenas.PREFIX + ChatColor.GREEN + " Done! The arena is now ready for use!");
                }
                PlatinumArenas.INSTANCE.getLogger().info("Arena created in " + tookS + " over " + data.tick + " ticks.");
            }
        };

        loopyCreate(data, ConfigManager.BLOCKS_ANALYZED_PER_SECOND / 20, player, runnable);

        return arena;
    }

    public boolean isBeingReset() {
        return beingReset;
    }

    public boolean isBeingCanceled() {
        return cancelReset;
    }

    public void cancelReset() {
        if (isBeingReset()) cancelReset = true;
    }

    /**
     * Small class that contains all the data we need to carry over between ticks
     * when resetting an arena.
     */
    private static class CreationLoopinData {
        List<Section> sections;
        List<Location> sectionStarts, sectionEnds;
        int tick, index, totalBlocks, maxBlocks;
        long lastUpdate;
        Arena arena;
        short[] blockAmounts, blockTypes = new short[0];
    }

    private static class ResetLoopinData {
        Map<Integer, Integer> sections = new HashMap<>();
        int currentSectionResetting;
        int blocksThisTick = 0;
        int maxBlocksThisTick;
        int speed;
        CommandSender sender;
        long overloadWarning;
        long calculateMicroseconds;
        long resetMicroseconds;
    }

    /**
     * Create an arena with recursion and delays to stop serious lag.
     * @param data The data of the arena being built
     * @param amount The amount of blocks to check in this tick
     * @param player The player creating the arena
     * @param onFinished Runnables to run after this is done
     */
    private static void loopyCreate(CreationLoopinData data, final int amount, Player player, Runnable onFinished) {
        Location start = data.sectionStarts.get(0);
        Location end = data.sectionEnds.get(0);
        int width = end.getBlockX() - start.getBlockX() + 1;
        int height = end.getBlockY() - start.getBlockY() + 1;
        int length = end.getBlockZ() - start.getBlockZ() + 1;
        List<BlockData> keyList =  new ArrayList<BlockData>(Arrays.asList(data.arena.keys));

        for (int i = 0; i < amount; i++) {
            Location loc;
            try {
                loc = Arena.getLocationAtIndex(width, height, length, data.arena.corner1.getWorld(), data.index);
            } catch (ArithmeticException e) {
                e.printStackTrace();
                PlatinumArenas.INSTANCE.getLogger().info("Debug for error: " + width + " x " + height + " x " + length);
                PlatinumArenas.INSTANCE.getLogger().info("Debug for error2: " +start + " | " + end + " || " + data.arena.getCorner1() + " | " + data.arena.getCorner2());
                return;
            }

            loc = start.clone().add(loc);
            if (data.index >= width * height * length) {
                //PlatinumArenas.INSTANCE.getLogger().info("Debug3: We are at the end");
                data.sections.add(new Section(data.arena, data.sections.size(), start, end, data.blockTypes, data.blockAmounts));
                data.blockAmounts = new short[0];
                data.blockTypes = new short[0];
                data.sectionStarts.remove(0);
                data.sectionEnds.remove(0);
                if (keyList.size() > data.arena.keys.length) data.arena.addKeys(keyList);

                if (data.sectionStarts.size() == 0) {
                    onFinished.run();
                    return;
                }

                start = data.sectionStarts.get(0);
                end = data.sectionEnds.get(0);
                width = end.getBlockX() - start.getBlockX() + 1;
                height = end.getBlockY() - start.getBlockY() + 1;
                length = end.getBlockZ() - start.getBlockZ() + 1;
                data.index = 0;

                continue;
            }

            if (!keyList.contains(loc.getBlock().getBlockData())) {
                keyList.add(loc.getBlock().getBlockData());
                //PlatinumArenas.INSTANCE.getLogger().info("Debug2: Adding " + loc.getBlock().getBlockData().getAsString() + " to keylist");
            }

            short index = (short)keyList.indexOf(loc.getBlock().getBlockData());
            //PlatinumArenas.INSTANCE.getLogger().info("Debug6: Index of " + loc.getBlock().getBlockData().getAsString() + " is " + index + "(" + keyList.indexOf(loc.getBlock().getBlockData()) + ")");

            if (data.blockTypes.length == 0) {
                data.blockAmounts = new short[] {1};
                data.blockTypes = new short[] {index};
                //PlatinumArenas.INSTANCE.getLogger().info("Debug4: Creating array thingies");
                if (keyList.size() > data.arena.keys.length) data.arena.addKeys(keyList);
                data.totalBlocks++;
                data.index++;
                updatePlayer(player, data);
                continue;
            }

            if (data.blockTypes[data.blockTypes.length - 1] == index) { //If the last block recorded is the same type
                data.blockAmounts[data.blockAmounts.length - 1] = (short) (data.blockAmounts[data.blockAmounts.length - 1] + 1);
                //PlatinumArenas.INSTANCE.getLogger().info("Debug5: Same type. Amount is now " + data.blockAmounts[data.blockAmounts.length - 1]);
                if (keyList.size() > data.arena.keys.length) data.arena.addKeys(keyList);
                data.index++;
                data.totalBlocks++;
                updatePlayer(player, data);
                continue;
            }

            data.blockAmounts = ArrayUtils.add(data.blockAmounts, (short)1);
            data.blockTypes = ArrayUtils.add(data.blockTypes, index);

            //TODO Preventive measures need to be put in place to stop the key list rising above the max short

            data.index++;
            data.totalBlocks++;

            updatePlayer(player, data);
        }
        data.tick++;
        if (keyList.size() > data.arena.keys.length) data.arena.addKeys(keyList);
        //PlatinumArenas.INSTANCE.getLogger().info("Debug5: new tick time");

        new BukkitRunnable() {
            @Override
            public void run() {
                loopyCreate(data, amount, player, onFinished);
            }
        }.runTaskLater(PlatinumArenas.INSTANCE, 1L);
    }

    private static void updatePlayer(Player player, CreationLoopinData data) {
        if (System.currentTimeMillis() - data.lastUpdate > 10 * 1000) {
            double perc = ((double)data.totalBlocks / (double)data.maxBlocks);
            NumberFormat format = NumberFormat.getPercentInstance();
            format.setMinimumFractionDigits(2);
            String percS = format.format(perc);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GREEN + "Arena " + percS + " analyzed (" + NumberFormat.getInstance().format(data.totalBlocks) + " blocks)");
            }
            PlatinumArenas.INSTANCE.getLogger().info("Arena " + percS + " analyzed (" + NumberFormat.getInstance().format(data.totalBlocks) + " blocks)");
            data.lastUpdate = System.currentTimeMillis();
        }
    }



    public BlockData[] getKeys() {
        return keys;
    }

    public short[] getBlocks() {
        return blocks;
    }

    public String getName() {
        return name;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public UUID getCreator() {
        return creator;
    }

    public void setCreator(UUID creator) {
        this.creator = creator;
    }

    /**
     * @return The height of the arena on the Y axis
     */
    public int getHeight() {
        return corner2.getBlockY() - corner1.getBlockY() + 1;
    }

    /**
     * @return The width of the arena on the X axis
     */
    public int getWidth() {
        return corner2.getBlockX() - corner1.getBlockX() + 1;
    }

    /**
     * @return The length of the arena on the Z axis
     */
    public int getLength() {
        return corner2.getBlockZ() - corner1.getBlockZ() + 1;
    }

    /**
     * @return The total number of blocks in the arena
     */
    public int getTotalBlocks() {
        return getWidth() * getHeight() * getLength();
    }

    /**
     * Returns a relative location object for where the provided index is in the arena. Quick math!
     * @param width Width
     * @param height Height
     * @param length Length
     * @param world World
     * @param index Arena index
     * @return
     */
    public static Location getLocationAtIndex(int width, int height, int length, World world, int index) {
        int x = index % width;
        int y = index / (length * width);
        int z = (index / width) % length;

        return new Location(world, x, y, z);
    }

    public List<Section> getSections() {
        return sections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return name.equals(arena.name) &&
                Objects.equals(creator, arena.creator) &&
                Arrays.equals(keys, arena.keys) &&
                corner1.equals(arena.corner1) &&
                corner2.equals(arena.corner2) &&
                properties.equals(arena.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, creator, corner1, corner2, properties);
        result = 31 * result + Arrays.hashCode(keys);
        return result;
    }
}
