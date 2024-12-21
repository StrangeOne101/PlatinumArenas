package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Banner;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BannerWrapper implements Wrapper<Banner, BannerWrapper.InternalBanner> {

    @Override
    public byte[] write(InternalBanner banner) {
        SmartWriter bb = new SmartWriter();
        bb.writeByte((byte)banner.color.ordinal());
        bb.writeByte((byte)banner.patterns.size());

        for (Pattern pattern : banner.patterns) {
            String identifier = pattern.getPattern().getKey().asString();
            bb.writeByte((byte)pattern.getColor().ordinal());
            bb.writeString(identifier);
        }
        bb.writeByteArray(banner.persistentData);
        return bb.toByteArray();
    }

    @Override
    public InternalBanner cache(Banner baseTileState) {
        InternalBanner banner = new InternalBanner();
        banner.color = baseTileState.getBaseColor();
        banner.patterns = baseTileState.getPatterns();
        try {
            banner.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return banner;
    }

    @Override
    public InternalBanner read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        InternalBanner banner = new InternalBanner();

        DyeColor color = DyeColor.values()[buffer.get()];
        banner.color = color;

        byte amount = buffer.get();

        for (int i = 0; i < amount; i++) {
            color = DyeColor.values()[buffer.get()];
            String id = buffer.getString();
            Pattern pattern = new Pattern(color, Registry.BANNER_PATTERN.get(NamespacedKey.fromString(id)));
            banner.patterns.add(pattern);
        }
        banner.persistentData = buffer.getByteArray();

        return banner;
    }

    @Override
    public Banner place(Banner baseTileState, InternalBanner cache) {
        baseTileState.setBaseColor(cache.color);
        baseTileState.setPatterns(cache.patterns);
        try {
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseTileState;
    }

    @Override
    public Class<Banner> getTileClass() {
        return Banner.class;
    }

    @Override
    public boolean isBlank(Banner tileState) {
        return tileState.numberOfPatterns() == 0 && tileState.getPersistentDataContainer().isEmpty();
    }

    protected static class InternalBanner {
        private DyeColor color;
        private List<Pattern> patterns = new ArrayList<>();
        private byte[] persistentData = new byte[0];

        @Override
        public String toString() {
            return "BannerData{" +
                    "color=" + color +
                    ", patterns=" + patterns +
                    '}';
        }
    }
}
