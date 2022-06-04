package com.strangeone101.platinumarenas.blockentity;

import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BannerWrapper implements Wrapper<Banner, BannerWrapper.InternalBanner> {

    @Override
    public byte[] write(InternalBanner banner) {
        ByteBuffer bb = ByteBuffer.allocate(2 + (banner.patterns.size() * 4));
        bb.put((byte)banner.color.ordinal());
        bb.put((byte)banner.patterns.size());

        for (Pattern pattern : banner.patterns) {
            String identifier = pattern.getPattern().getIdentifier();
            while (identifier.length() < 3) {
                identifier = identifier + " ";
            }
            bb.put((byte)pattern.getColor().ordinal());
            byte[] identifierBytes = identifier.getBytes(StandardCharsets.US_ASCII);
            for (byte b : identifierBytes) bb.put(b);
        }
        return bb.array();
    }

    @Override
    public InternalBanner cache(Banner baseTileState) {
        InternalBanner banner = new InternalBanner();
        banner.color = baseTileState.getBaseColor();
        banner.patterns = baseTileState.getPatterns();
        return banner;
    }

    @Override
    public InternalBanner read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.position(0);

        InternalBanner banner = new InternalBanner();

        DyeColor color = DyeColor.values()[buffer.get()];
        banner.color = color;

        byte amount = buffer.get();

        for (int i = 0; i < amount; i++) {
            color = DyeColor.values()[buffer.get()];
            byte[] idBytes = {buffer.get(), buffer.get(), buffer.get()};
            String id = new String(idBytes, StandardCharsets.US_ASCII).trim();
            Pattern pattern = new Pattern(color, PatternType.getByIdentifier(id));
            banner.patterns.add(pattern);
        }

        return banner;
    }

    @Override
    public Banner place(Banner baseTileState, InternalBanner cache) {
        baseTileState.setBaseColor(cache.color);
        baseTileState.setPatterns(cache.patterns);
        return baseTileState;
    }

    @Override
    public Class<Banner> getTileClass() {
        return Banner.class;
    }

    @Override
    public boolean isBlank(Banner tileState) {
        return tileState.numberOfPatterns() == 0;
    }

    protected static class InternalBanner {
        private DyeColor color;
        private List<Pattern> patterns = new ArrayList<>();
    }
}
