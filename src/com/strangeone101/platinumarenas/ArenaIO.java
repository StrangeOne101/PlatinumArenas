package com.strangeone101.platinumarenas;

import com.google.common.base.Ascii;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import com.strangeone101.platinumarenas.blockentity.Wrapper;
import com.strangeone101.platinumarenas.blockentity.WrapperRegistry;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArenaIO {

    private static final byte SECTION_SPLIT = '\u0002';
    private static final byte KEY_SPLIT = '\u0003';
    private static final byte ARENASECTION_SPLIT = '\u0004';

    public static final int FILE_VERSION = 4;

    /**
     * Saves an arena to disk.
     * @param file The File to save to
     * @param arena The arena to save
     * @param callback Any callback functions you want to run after this
     *                 completes (this doesn't run async so why did I do this?)
     * @return True if the arena saved.
     */
    public static boolean saveArena(File file, final Arena arena, Runnable... callback) {
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();

        if (corner1.getWorld() != corner2.getWorld()) return false;

        try {
            FileOutputStream stream = new FileOutputStream(file);
            Location l = arena.getCorner1();
            Location l2 = arena.getCorner2();
            byte split = ',';

            //HEADER SECTION
            String header = FILE_VERSION + "," + arena.getName() + "," + l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," +
                    l2.getBlockX() + "," + l2.getBlockY() + "," + l2.getBlockZ() + "," + arena.getCreator().toString() + "," + PlatinumArenas.INSTANCE.getMCVersion()
                    + "," + arena.getCreationTime();
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);

            //BLOCKDATA KEY SECTION

            byte[] keyBytes = new byte[0]; //A byte array to store all the blocklist keys. They are separated by the KEY_SPLIT char

            for (BlockData data : arena.getKeys()) {
                String s = data.getAsString(true); //Convert BlockData into string
                keyBytes = ArrayUtils.addAll(keyBytes, s.getBytes(StandardCharsets.US_ASCII)); //Then to bytes in ascii
                keyBytes = ArrayUtils.add(keyBytes, KEY_SPLIT); //Add splitting character
            }

            keyBytes = ArrayUtils.remove(keyBytes, keyBytes.length - 1); //Remove last splitter character

            //BLOCK SECTION

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            short sections = (short) arena.getSections().size(); //Amount of sections

            ByteBuffer sb = ByteBuffer.allocate(2); //Allows us to convert other primitives to bytes easily
            sb.putShort(sections); //Put a single short at the start that is the amount of sections. Means the max number of sections is 32k

            byteStream.write(sb.array()); //Write to array

            for (int s = 0; s < arena.getSections().size(); s++) {
                Section section = arena.getSections().get(s);

                byte[] nbtBytes = section.getNBTData();

                //7 ints (2x XYZ coords + size of the block types) + block types and amounts in bytes (2 shorts = 4 bytes))
                //Since version 4, we have the length of the NBT bytes as well.
                ByteBuffer ib = ByteBuffer.allocate((7 * 4) + (section.getBlockAmounts().length * 4) + nbtBytes.length);

                ib.putInt(section.getStart().getBlockX());
                ib.putInt(section.getStart().getBlockY());
                ib.putInt(section.getStart().getBlockZ());
                ib.putInt(section.getEnd().getBlockX());
                ib.putInt(section.getEnd().getBlockY());
                ib.putInt(section.getEnd().getBlockZ());

                ib.putInt(section.getBlockTypes().length * 2); //Amount of block data to write

                //ib.put((byte)nbtBytes.length); //How many NBT tile entity types we have

                for(int i = 0; i < nbtBytes.length; i++) ib.put(nbtBytes[i]); //Write the NBT

                for (int i = 0; i < section.getBlockAmounts().length; i++) {
                    ib.putShort(section.getBlockAmounts()[i]);
                    ib.putShort(section.getBlockTypes()[i]);
                }

                byteStream.write(ib.array());

                //Don't write the arena section split as of version 4
                /*if (s < arena.getSections().size() - 1) { //If there is more sections to go, put the splitter
                    byteStream.write(new byte[] {0x00, 0x00, 0x00, ARENASECTION_SPLIT});
                }*/
            }

           /* short amount = 1;
            short type = -1;
            List<Short> blockList = new ArrayList<>();

            for (int i = 0; i < arena.getBlocks().length; i++) {
                short currType = arena.getBlocks()[i];

                if (currType == type) {
                    amount++;
                } else if (currType != type && i != 0) {
                    blockList.add(amount);
                    blockList.add(type);

                    amount = 1;
                    type = currType;
                } else if (i == 0) {
                    type = currType;
                }

                if (i == arena.getBlocks().length - 1) { //End of the line. Write all we got
                    blockList.add(amount);
                    blockList.add(type);
                }
            }

            ByteBuffer blockByteBuffer = ByteBuffer.allocate(blockList.size() * 2);
            for (short s : blockList) {
                blockByteBuffer.putShort(s);
            }*/



            byte[] blockBytes = byteStream.toByteArray();

            //Combine all the different sections together into a single byte array to write to file
            byte[] totalBytes = new byte[0];
            totalBytes = ArrayUtils.addAll(totalBytes, headerBytes);
            totalBytes = ArrayUtils.add(totalBytes, SECTION_SPLIT);
            totalBytes = ArrayUtils.addAll(totalBytes, keyBytes);
            totalBytes = ArrayUtils.add(totalBytes, SECTION_SPLIT);
            totalBytes = ArrayUtils.addAll(totalBytes, blockBytes);

            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".datc")) {
                totalBytes = Util.compress(totalBytes);
            }

            stream.write(totalBytes);
            stream.close();

            for (Runnable r : callback) r.run();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    /**
     * Loads an Arena object from the provided file
     * @param file The arena file to load
     * @return New Arena object
     */
    public static Arena loadArena(File file) {
        try {
            byte[] readBytes = Files.readAllBytes(file.toPath());

            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".datc")) { //.datc is the compressed arena format
                readBytes = Util.decompress(readBytes);
            }

            //HEADER SECTION
            int firstSectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT);
            byte[] header = Arrays.copyOfRange(readBytes, 0, firstSectionSplit);
            String headerString = new String(header, StandardCharsets.US_ASCII);

            String name = "**BrokenArena**";
            Location corner1 = null;
            Location corner2 = null;
            UUID owner = PlatinumArenas.DEFAULT_OWNER;
            long created = 0;
            int arenaMCVersion = getVersion("1.15.2"); //The default for when the version isn't in the file
            int currentMCVersion = getVersion(PlatinumArenas.INSTANCE.getMCVersion());

            int version = Integer.parseInt(headerString.split(",")[0]);

            if (version >= 4) {
                created = Long.parseLong(headerString.split(",")[11]);
            }
            if (version >= 3) {
                arenaMCVersion = getVersion(headerString.split(",")[10]);
            }
            if (version >= 2) {
                owner = UUID.fromString(headerString.split(",")[9]);
            }
            if (version >= 1) {
                name = headerString.split(",")[1];
                String world = headerString.split(",")[2];
                int x1 = Integer.parseInt(headerString.split(",")[3]);
                int y1 = Integer.parseInt(headerString.split(",")[4]);
                int z1 = Integer.parseInt(headerString.split(",")[5]);
                int x2 = Integer.parseInt(headerString.split(",")[6]);
                int y2 = Integer.parseInt(headerString.split(",")[7]);
                int z2 = Integer.parseInt(headerString.split(",")[8]);

                World realWorld = Bukkit.getWorld(world);

                if (realWorld == null) {
                    PlatinumArenas.INSTANCE.getLogger().warning("Could not locate world \"" + world + "\" for arena \"" + name + "\"! Using default world");
                    realWorld = Bukkit.getWorlds().get(0);
                }

                corner1 = new Location(realWorld, x1, y1, z1);
                corner2 = new Location(realWorld, x2, y2, z2);
            }

            if (Arena.arenas.containsKey(name)) {
                PlatinumArenas.INSTANCE.getLogger().warning("Tried to load arena \"" + name + "\" twice! Did you duplicate the file?");
                return null;
            }

            //KEY SECTION
            int keySectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT, firstSectionSplit + 1);
            byte[] keyBytes = Arrays.copyOfRange(readBytes, firstSectionSplit + 1, keySectionSplit);
            //PlatinumArenas.INSTANCE.getLogger().info("Keybyte size = " + keyBytes.length);
            String currentKey = "";


            List<BlockData> blockDataSet = new ArrayList<>();

            /*for (int i = 0; i < keyBytes.length; i++) {
                byte currByte = keyBytes[i];

                if (currByte == KEY_SPLIT || i == keyBytes.length - 1) {
                    blockDataSet.add(Bukkit.createBlockData(currentKey));
                    currentKey = "";
                }



                //TODO currentKey is not actually set with the new currByte
            }*/

            for (byte[] key : Util.split(new byte[] {KEY_SPLIT}, keyBytes)) {
                String blockData = new String(key, StandardCharsets.US_ASCII);
                //PlatinumArenas.INSTANCE.getLogger().info("Loaded block key: " + blockData);

                String material = blockData.split("\\[")[0];

                //Since Mojang are amazing and rename blocks (WHY?????)
                if (arenaMCVersion < 1170 && currentMCVersion >= 1170 && material.equalsIgnoreCase("minecraft:grass_path")) { //Was renamed in 1.17
                    blockData = "minecraft:dirt_path";
                } else if (arenaMCVersion < 1170 && currentMCVersion >= 1170 && material.equalsIgnoreCase("minecraft:cauldron")) {
                    if (blockData.contains("[")) { //Contains the water level. If it doesn't, then it's empty
                        blockData = "minecraft:water_cauldron" + blockData.split("\\[", 2)[1];
                    }
                } else if (arenaMCVersion < 1150 && currentMCVersion >= 1150 && material.equalsIgnoreCase("minecraft:beetroots")) { //Beetroot had their age halved
                    if (blockData.contains("[")) { //Contains the age
                        Pattern pattern = Pattern.compile("minecraft:beetroots\\[age=(\\d)\\]");
                        Matcher matcher = pattern.matcher(blockData);
                        int age = Integer.parseInt(matcher.group());
                        int newAge = 0;
                        if (age == 7) newAge = 3;
                        else if (age >= 4 && age <= 6) newAge = 2;
                        else if (age >= 1 && age <= 3) newAge = 1;
                        blockData = "minecraft:beetroots[age=" + newAge + "]";
                    }
                } else if (arenaMCVersion < 1160 && currentMCVersion >= 1160 && Util.isPre116Wall(material)) { //Convert old walls
                    HashMap<String, String> mapData = (HashMap<String, String>) Util.convertBlockstateData(blockData);
                    Map<String, String> mapDataClone = (Map<String, String>) mapData.clone();
                    mapDataClone.remove("up");
                    mapDataClone.remove("waterlogged");
                    mapDataClone.entrySet().stream().forEach((entry) -> {
                        if (entry.getValue().equalsIgnoreCase("true")) mapData.put(entry.getKey(), "low");
                        else mapData.put(entry.getKey(), "none");
                    });

                    blockData = material + "[" + Util.convertBlockstateData(mapData) + "]";
                }
                try {
                    BlockData bukkitData = Bukkit.createBlockData(blockData);
                    blockDataSet.add(bukkitData);
                } catch (IllegalArgumentException e) {
                    PlatinumArenas.INSTANCE.getLogger().severe("Failed to parse block data '" + blockData + "' for arena '" + name + "'!");
                    blockDataSet.add(Bukkit.createBlockData(blockData.split("\\[")[0]));
                }
            }

            Arena arena = new Arena(name, corner1, corner2);
            arena.setCreator(owner);
            arena.setCreationTime(created);
            if (version >= 3) {
                arena.setMcVersion(headerString.split(",")[10]);
            } else {
                arena.setMcVersion("Unknown");
            }
            arena.setFileVersion(version);

            arena.setKeys(blockDataSet);

            //BLOCK SECTION
            //int blockSectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT, keySectionSplit + 1);
            byte[] blockBytes = Arrays.copyOfRange(readBytes, keySectionSplit + 1, readBytes.length);

            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.put(blockBytes[0]);
            bb.put(blockBytes[1]);
            short sectionCount = bb.getShort(0);
            short currentSection = 0;

            byte[] arenaSectionSplit = {0x00, 0x00, 0x00, ARENASECTION_SPLIT};

            blockBytes = Arrays.copyOfRange(blockBytes, 2, blockBytes.length); //Cut the 2 bytes off at the front
            //PlatinumArenas.INSTANCE.getLogger().info(blockBytes.length + " bytes in blockBytes");

            ByteBuffer buffer = ByteBuffer.allocate(blockBytes.length);
            buffer.put(blockBytes);
            buffer.position(0);

            while (currentSection < sectionCount) {
                int x1 = buffer.getInt();
                int y1 = buffer.getInt();
                int z1 = buffer.getInt();
                int x2 = buffer.getInt();
                int y2 = buffer.getInt();
                int z2 = buffer.getInt();
                Location start = new Location(corner1.getWorld(), x1, y1, z1);
                Location end = new Location(corner1.getWorld(), x2, y2, z2);
                int left = buffer.getInt();

                Map<Integer, Pair<Wrapper, Object>> NBT = Maps.newHashMapWithExpectedSize(0);

                if (version >= 4) { //Version 4 adds the basic NBT support
                    NBT = new HashMap<>();

                    byte differentTypes = buffer.get(); //Get the amount of wrapper types in this section
                    for (int i = 0; i < differentTypes; i++) {
                        int id = buffer.get();
                        int amount = buffer.getShort(); //Get amount of blocks with NBT data for this ID in this section

                        Wrapper wrapper = WrapperRegistry.getFromId(id); //Get wrapper from ID

                        for (int j = 0; j < amount; j++) {

                            byte amountOfIndexes = buffer.get(); //The amount of indexes
                            int[] indexes = new int[amountOfIndexes];

                            for (int k = 0; k < amountOfIndexes; k++) { //Get the list of indexes
                                indexes[k] = buffer.getInt();
                            }

                            int dataLength = buffer.getInt(); //Get the length of the data to cache
                            byte[] dataBytes = new byte[dataLength];
                            for (int k = 0; k < dataLength; k++) dataBytes[k] = buffer.get(); //Read it

                            Object cache = wrapper.read(dataBytes); //Convert bytes to object to store in memory

                            for (int index : indexes) {
                                NBT.put(index, new ImmutablePair<>(wrapper, cache));
                            }
                        }
                    }
                }

                short[] amounts = new short[left / 2];
                short[] types = new short[left / 2];

                for (int i = 0; i < left / 2; i++) {
                    amounts[i] = buffer.getShort();
                    types[i] = buffer.getShort();
                }

                Section section = new Section(arena, currentSection, start, end, types, amounts, NBT);
                arena.getSections().add(section);
                currentSection++;

                //The arena section split is no longer added to arenas on version 4 or later
                if (version < 4 && currentSection != sectionCount) {
                    buffer.getInt(); //Skip the arena section split
                }


            }

            /*List<byte[]> bytesForSections = Util.split(arenaSectionSplit, blockBytes);
            PlatinumArenas.INSTANCE.getLogger().info(bytesForSections.size() + " sections in arena.");

            for (byte[] sectionBytes : bytesForSections) {
                ByteBuffer buffer = ByteBuffer.allocate(sectionBytes.length);
                buffer.put(sectionBytes);
                buffer.position(0);
                int x1 = buffer.getInt();
                int y1 = buffer.getInt();
                int z1 = buffer.getInt();
                int x2 = buffer.getInt();
                int y2 = buffer.getInt();
                int z2 = buffer.getInt();

                Location start = new Location(corner1.getWorld(), x1, y1, z1);
                Location end = new Location(corner1.getWorld(), x2, y2, z2);

                int left = buffer.getInt();
                PlatinumArenas.INSTANCE.getLogger().info(left + " shortsets in this section");
                short[] amounts = new short[left / 2];
                short[] types = new short[left / 2];
                int i = 0;
                while (buffer.hasRemaining()) {
                    amounts[i] = buffer.getShort();
                    types[i] = buffer.getShort();
                    i++;
                }

                Section section = new Section(arena, start, end, types, amounts);
                arena.getSections().add(section);
            }*/


            /*ShortBuffer sb = ShortBuffer.allocate(blockBytes.length / 2);
            short[] blockList = new short[]
            while (sb.hasRemaining()) {
                short amount = sb.get();
                short type = sb.get();

                for (int i = 0; i < amount; i++) {
                    blockList.add(type);
                }
            }*/

            //ARENA RECREATION


            //arena.merge((BlockData[]) blockDataSet.toArray(), blockList.stream().map((Short) i->(short)i).toArray());

            return arena;

        } catch (Exception e) {
            PlatinumArenas.INSTANCE.getLogger().warning("Failed to load arena file \"" + file.getName() + "\"!");
            e.printStackTrace();
        }

        return null;  //unfinished
    }

    private static int getVersion(String version) {

        if (!version.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            PlatinumArenas.INSTANCE.getLogger().warning("Version not valid! Cannot parse version \"" + version + "\"");

            return 1164; //1.16.4
        }

        String[] split = version.split("\\.", 3);

        int major = Integer.parseInt(split[0]);
        int minor = 0;
        int fix = 0;

        if (split.length > 1) {
            minor = Integer.parseInt(split[1]);

            if (split.length > 2) {
                fix = Integer.parseInt(split[2]);
            }
        }

        return major * 1000 + minor * 10 + fix; //1.16.4 -> 1164; 1.18 -> 1180
    }

    /**
     * Unloads the current arenas and loads them all from file again.
     * @return The list of arenas
     */
    public static Collection<Arena> loadAllArenas() {
        File folder = new File(PlatinumArenas.INSTANCE.getDataFolder(), "Arenas");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Arena.arenas.clear();
        long time = System.currentTimeMillis();

        for (File f : folder.listFiles()) {
            if (f.getName().toLowerCase(Locale.ROOT).endsWith(".dat") || f.getName().toLowerCase(Locale.ROOT).endsWith(".datc")) {
                try {
                    Arena arena = ArenaIO.loadArena(f);
                    if (arena == null) continue;
                    PlatinumArenas.INSTANCE.getLogger().info("Loaded arena \"" + arena.getName() + "\" from file " + f.getName());
                    Arena.arenas.put(arena.getName(), arena);
                } catch (Exception e) {
                    PlatinumArenas.INSTANCE.getLogger().warning("Failed to load arena file \"" + f.getName() + "\"!");
                    e.printStackTrace();
                }
            }
        }
        long took = System.currentTimeMillis() - time;

        PlatinumArenas.INSTANCE.getLogger().info("Loaded " + Arena.arenas.size() + " arenas in " + took + "ms!");
        PlatinumArenas.INSTANCE.ready = true;

        return Arena.arenas.values();
    }
}
