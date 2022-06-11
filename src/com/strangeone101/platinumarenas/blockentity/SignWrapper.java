package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.PlatinumArenas;
import org.apache.commons.lang.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        byte color = (byte)sign.color.ordinal();

        if (sign.glow) {
            color += Byte.MAX_VALUE;
        }

        out.write((byte)sign.color.ordinal());
        out.write((byte)sign.lines.size());
        for (String s : sign.lines) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
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
        sign.lines = Arrays.asList(baseTileState.getLines());
        return sign;
    }

    @Override
    public InternalSign read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(0);

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
            int stringLength = buffer.getInt();
            byte[] byteArray = new byte[stringLength];
            for (int j = 0; j < stringLength; j++) {
                byteArray[j] = buffer.get();
            }
            String line = new String(byteArray, StandardCharsets.UTF_8);
            cache.lines.add(line);
        }
        return cache;
    }

    @Override
    public Sign place(Sign baseTileState, InternalSign cache) {
        baseTileState.setColor(cache.color);
        for (int i = 0; i < cache.lines.size(); i++) {
            baseTileState.setLine(i, cache.lines.get(i));
        }
        if (mcVersion >= 1170) baseTileState.setGlowingText(cache.glow);
        return baseTileState;
    }

    @Override
    public Class<Sign> getTileClass() {
        return Sign.class;
    }

    @Override
    public boolean isBlank(Sign tileState) {
        return tileState.getLines().length == 0 || Arrays.stream(tileState.getLines()).allMatch(StringUtils::isEmpty);
    }

    protected static class InternalSign {
        private DyeColor color;
        private List<String> lines = new ArrayList<>();
        private boolean glow = false;
    }
}
