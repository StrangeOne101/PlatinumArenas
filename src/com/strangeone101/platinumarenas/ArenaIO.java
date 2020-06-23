package com.strangeone101.platinumarenas;

import com.google.common.base.Ascii;
import com.google.common.primitives.Primitives;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.List;
import java.util.UUID;

public class ArenaIO {

    private static final byte SECTION_SPLIT = '\u0002';
    private static final byte KEY_SPLIT = '\u0003';
    private static final byte ARENASECTION_SPLIT = '\u0004';

    private static final int FILE_VERSION = 1;


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
            String header = FILE_VERSION + "," + arena.getName() + "," + l.getWorld() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," +
                    l2.getBlockX() + "," + l2.getBlockY() + "," + l2.getBlockZ();
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);

            //BLOCKDATA KEY SECTION

            byte[] keyBytes = new byte[0];

            for (BlockData data : arena.getKeys()) {
                String s = data.getAsString(true); //Convert BlockData into string
                keyBytes = ArrayUtils.addAll(keyBytes, s.getBytes(StandardCharsets.US_ASCII)); //Then to bytes in ascii
                keyBytes = ArrayUtils.add(keyBytes, KEY_SPLIT); //Add splitting character
            }

            keyBytes = ArrayUtils.remove(keyBytes, keyBytes.length - 1); //Remove last splitter character

            //BLOCK SECTION

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            short sections = (short) arena.getSections().size(); //Amount of sections

            ByteBuffer sb = ByteBuffer.allocate(2);
            sb.putShort(sections);

            byteStream.write(sb.array()); //Write to array

            for (int s = 0; s < arena.getSections().size(); s++) {
                Section section = arena.getSections().get(s);

                ByteBuffer ib = ByteBuffer.allocate((7 * 4) + (section.getBlockAmounts().length * 4));

                ib.putInt(section.getStart().getBlockX());
                ib.putInt(section.getStart().getBlockY());
                ib.putInt(section.getStart().getBlockZ());
                ib.putInt(section.getEnd().getBlockX());
                ib.putInt(section.getEnd().getBlockY());
                ib.putInt(section.getEnd().getBlockZ());

                ib.putInt(section.getBlockTypes().length * 2); //Amount of data to write

                for (int i = 0; i < section.getBlockAmounts().length; i++) {
                    ib.putShort(section.getBlockAmounts()[i]);
                    ib.putShort(section.getBlockTypes()[i]);
                }

                byteStream.write(ib.array());

                if (s < arena.getSections().size()) { //If there is more sections to go, put the splitter
                    byteStream.write(new byte[] {0x00, ARENASECTION_SPLIT});
                }

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

            byte[] totalBytes = new byte[0];
            totalBytes = ArrayUtils.addAll(totalBytes, headerBytes);
            totalBytes = ArrayUtils.add(totalBytes, SECTION_SPLIT);
            totalBytes = ArrayUtils.addAll(totalBytes, keyBytes);
            totalBytes = ArrayUtils.add(totalBytes, SECTION_SPLIT);
            totalBytes = ArrayUtils.addAll(totalBytes, blockBytes);

            stream.write(totalBytes);
            stream.close();

            for (Runnable r : callback) r.run();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public static Arena loadArena(File file) {
        try {

            byte[] readBytes = Files.readAllBytes(file.toPath());

            //HEADER SECTION
            int firstSectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT);
            byte[] header = Arrays.copyOfRange(readBytes, 0, firstSectionSplit);
            String headerString = new String(header, StandardCharsets.US_ASCII);

            String name = "**BrokenArena**";
            Location corner1 = null;
            Location corner2 = null;
            UUID owner = PlatinumArenas.DEFAULT_OWNER;

            int version = Integer.parseInt(headerString.split(",")[0]);

            if (version >= 2) {
                owner = UUID.fromString(headerString.split(",")[9]);
            }
            if (version >= 1) {
                name = headerString.split(",")[1];
                String world = name = headerString.split(",")[2];
                int x1 = Integer.parseInt(headerString.split(",")[3]);
                int y1 = Integer.parseInt(headerString.split(",")[4]);
                int z1 = Integer.parseInt(headerString.split(",")[5]);
                int x2 = Integer.parseInt(headerString.split(",")[6]);
                int y2 = Integer.parseInt(headerString.split(",")[7]);
                int z2 = Integer.parseInt(headerString.split(",")[8]);

                corner1 = new Location(Bukkit.getWorld(world), x1, y1, z1);
                corner2 = new Location(Bukkit.getWorld(world), x2, y2, z2);
            }

            //KEY SECTION
            int keySectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT, firstSectionSplit + 1);
            byte[] keyBytes = Arrays.copyOfRange(readBytes, firstSectionSplit, keySectionSplit);

            String currentKey = "";
            List<BlockData> blockDataSet = new ArrayList<>();

            for (int i = 0; i < keyBytes.length; i++) {
                byte currByte = keyBytes[i];

                if (currByte == KEY_SPLIT || i == keyBytes.length - 1) {
                    blockDataSet.add(Bukkit.createBlockData(currentKey));
                    currentKey = "";
                }
            }

            Arena arena = new Arena(name, corner1, corner2);
            arena.setCreator(owner);

            //BLOCK SECTION
            int blockSectionSplit = ArrayUtils.indexOf(readBytes, SECTION_SPLIT, keySectionSplit + 1);
            byte[] blockBytes = Arrays.copyOfRange(readBytes, keySectionSplit, blockSectionSplit);

            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.put(blockBytes[0]);
            bb.put(blockBytes[1]);
            short sectionCount = bb.getShort(0);

            byte[] arenaSectionSplit = {0x00, ARENASECTION_SPLIT};

            blockBytes = Arrays.copyOfRange(blockBytes, 2, blockBytes.length); //Cut the 2 bytes off at the front

            List<byte[]> bytesForSections = Util.split(arenaSectionSplit, blockBytes);

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
            }


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



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;  //unfinished
    }
}
