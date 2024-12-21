package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignWrapper implements Wrapper<Sign, SignWrapper.InternalSign> {

    private int mcVersion;

    public SignWrapper() {
        mcVersion = PlatinumArenas.getIntVersion(PlatinumArenas.getMCVersion());
    }

    @Override
    public byte[] write(InternalSign sign) {
        SmartWriter out = new SmartWriter();
        byte color = (byte)sign.color.ordinal();

        if (sign.glow) {
            color += Byte.MAX_VALUE;
        }

        out.writeByte(color);
        out.writeByte((byte)sign.lines.size());
        for (String s : sign.lines) {
            out.writeString(s);
        }
        byte backColor = (byte)sign.backColor.ordinal();

        if (sign.backGlow) {
            backColor += Byte.MAX_VALUE;
        }
        out.writeByte(backColor);

        out.writeByteArray(sign.persistentData);

        return out.toByteArray();
    }

    @Override
    public InternalSign cache(Sign baseTileState) {
        InternalSign sign = new InternalSign();
        sign.color = baseTileState.getColor();
        if (mcVersion >= 1170) {
            if (baseTileState.isGlowingText()) {
                sign.glow = true;
            }
        }
        sign.lines = new ArrayList<>(List.of(baseTileState.getLines()));
        if (baseTileState.getSide(Side.BACK).getLines().length > 0) {
            sign.backColor = baseTileState.getSide(Side.BACK).getColor();
            if (baseTileState.getSide(Side.BACK).isGlowingText()) {
                sign.backGlow = true;
            }

            while (sign.lines.size() < 4) { //So the back lines don't get appended to the front
                sign.lines.add("");
            }

            //Add the back lines
            sign.lines.addAll(Arrays.asList(baseTileState.getSide(Side.BACK).getLines()));
        }

        try {
            sign.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sign;
    }

    @Override
    public InternalSign read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        InternalSign cache = new InternalSign();

        byte byteColor = buffer.get();
        if (byteColor < 0) {
            byteColor -= Byte.MAX_VALUE;
            cache.glow = true;
        }
        DyeColor color = DyeColor.values()[byteColor];
        cache.color = color;

        int length = buffer.get();

        for (int i = 0; i < length; i++) {
            String line = buffer.getString();
            cache.lines.add(line);
        }

        if (length > 4) { //If the length is more than 4, it has lines on the back
            byte byteBackColor = buffer.get();
            if (byteBackColor < 0) {
                byteBackColor -= Byte.MAX_VALUE;
                cache.backGlow = true;
            }
            DyeColor backColor = DyeColor.values()[byteBackColor];
            cache.backColor = backColor;
        }

        cache.persistentData = buffer.getByteArray();

        return cache;
    }

    @Override
    public Sign place(Sign baseTileState, InternalSign cache) {
        baseTileState.setColor(cache.color);
        for (int i = 0; i < Math.min(cache.lines.size(), 4); i++) {
            baseTileState.setLine(i, cache.lines.get(i));
        }
        if (mcVersion >= 1170) baseTileState.setGlowingText(cache.glow);
        if (mcVersion >= 1200 && cache.lines.size() > 4) {
            for (int i = 0; i < cache.lines.size() - 4; i++) {
                baseTileState.getSide(Side.BACK).setLine(i, cache.lines.get(i + 4));
            }
            baseTileState.getSide(Side.BACK).setColor(cache.backColor);
            baseTileState.getSide(Side.BACK).setGlowingText(cache.backGlow);
        }

        try {
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baseTileState;
    }

    @Override
    public Class<Sign> getTileClass() {
        return Sign.class;
    }

    @Override
    public boolean isBlank(Sign tileState) {
        return tileState.getLines().length == 0 || Arrays.stream(tileState.getLines()).allMatch(StringUtils::isEmpty) && tileState.getPersistentDataContainer().isEmpty();
    }

    protected static class InternalSign {
        private DyeColor color;
        private DyeColor backColor = DyeColor.BLACK;
        private List<String> lines = new ArrayList<>();
        private boolean glow = false;
        private boolean backGlow = false;
        private byte[] persistentData = new byte[0];

        @Override
        public String toString() {
            return "SignData{" +
                    "color=" + color +
                    ", backColor=" + backColor +
                    ", lines=" + lines +
                    ", glow=" + glow +
                    ", backGlow=" + backGlow +
                    '}';
        }
    }
}
