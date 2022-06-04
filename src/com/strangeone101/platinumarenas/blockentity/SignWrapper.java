package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

    @Override
    public byte[] write(InternalSign sign) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
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
        sign.lines = Arrays.asList(baseTileState.getLines());
        return sign;
    }

    @Override
    public InternalSign read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(0);

        InternalSign cache = new InternalSign();

        DyeColor color = DyeColor.values()[buffer.get()];
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
    }
}
