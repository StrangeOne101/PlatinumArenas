package com.strangeone101.platinumarenas;

import org.bukkit.Location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        if (pos + (pattern.length - 1) > input.length) return false;
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> split(byte[] pattern, byte[] input) {
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        for(int i=0; i<input.length; i++) {
            if(isMatch(pattern,input,i)) {
                l.add(Arrays.copyOfRange(input, blockStart, i));
                blockStart = i+pattern.length;
                i = blockStart;
            }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }

    public static boolean isLocationWithin(Location min, Location max, Location test) {
        int x1 = min.getBlockX();
        int x2 = max.getBlockX();
        int y1 = min.getBlockY();
        int y2 = max.getBlockY();
        int z1 = min.getBlockZ();
        int z2 = max.getBlockZ();

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

        if (test.getX() >= x1 && test.getX() <= x2 && test.getY() >= y1 && test.getY() <= y2 && test.getZ() >= z1 && test.getZ() <= z2) {
            return true;
        }

        return false;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Copies a resource located in the jar to a file.
     *
     * @param resourceName The filename of the resource to copy
     * @param output The file location to copy it to. Should not exist.
     * @return True if the operation succeeded.
     */
    public static boolean saveResource(String resourceName, File output) {
        if (PlatinumArenas.INSTANCE.getResource(resourceName) == null) return false;

        try {
            InputStream in = PlatinumArenas.INSTANCE.getResource(resourceName);

            OutputStream out = new FileOutputStream(output);
            byte[] buf = new byte[256];
            int len;

            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }

            out.close();
            in.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
