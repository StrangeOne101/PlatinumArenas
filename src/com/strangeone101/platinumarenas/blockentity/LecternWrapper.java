package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;

public class LecternWrapper implements Wrapper<Lectern, LecternWrapper.LecturnCache> {

    @Override
    public byte[] write(LecturnCache cache) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        if (cache.book == null) {
            out.writeInt(-1);
            return out.toByteArray();
        }
        out.writeInt(cache.page);
        byte[] itemBytes = cache.book.serializeAsBytes();
        out.writeInt(itemBytes.length);
        out.write(itemBytes);

        return out.toByteArray();
    }

    @Override
    public LecturnCache read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);

        LecturnCache cache = new LecturnCache();

        int page = buffer.getInt();
        if (page == -1) {
            return cache;
        }

        byte[] containerBytes = new byte[bytes.length - 4];
        buffer.get(containerBytes);

        cache.book = ItemStack.deserializeBytes(containerBytes);
        return cache;
    }

    @Override
    public LecturnCache cache(Lectern baseTileState) {
        LecturnCache cache = new LecturnCache();
        cache.book = baseTileState.getInventory().getItem(0);
        cache.page = baseTileState.getPage();
        return cache;
    }

    @Override
    public Lectern place(Lectern baseTileState, LecturnCache cache) {
        baseTileState.getInventory().setItem(0, cache.book);
        baseTileState.setPage(cache.page);
        return baseTileState;
    }

    @Override
    public Class<Lectern> getTileClass() {
        return Lectern.class;
    }

    @Override
    public boolean isBlank(Lectern tileState) {
        return tileState.getInventory().isEmpty();
    }


    public class LecturnCache {
        public int page;
        public ItemStack book;

        @Override
        public String toString() {
            return "LecturnCache{" +
                    "page=" + page +
                    ", book=" + book +
                    '}';
        }
    }
}
