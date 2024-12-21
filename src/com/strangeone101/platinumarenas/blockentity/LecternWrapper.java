package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;

public class LecternWrapper implements Wrapper<Lectern, LecternWrapper.LecturnCache> {

    @Override
    public byte[] write(LecturnCache cache) {
        SmartWriter out = new SmartWriter();

        if (cache.book == null) {
            out.writeInt(-1);
            return out.toByteArray();
        }
        out.writeInt(cache.page);
        byte[] itemBytes = cache.book.serializeAsBytes();
        out.writeByteArray(itemBytes);

        return out.toByteArray();
    }

    @Override
    public LecturnCache read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        LecturnCache cache = new LecturnCache();

        int page = buffer.getInt();
        if (page == -1) {
            return cache;
        }

        cache.page = page;
        cache.book = ItemStack.deserializeBytes(buffer.getByteArray());
        return cache;
    }

    @Override
    public LecturnCache cache(Lectern baseTileState) {
        LecturnCache cache = new LecturnCache();
        cache.book = baseTileState.getSnapshotInventory().getItem(0);
        cache.page = baseTileState.getPage();
        return cache;
    }

    @Override
    public Lectern place(Lectern baseTileState, LecturnCache cache) {
        baseTileState.getSnapshotInventory().setItem(0, cache.book);
        baseTileState.setPage(cache.page);
        return baseTileState;
    }

    @Override
    public Class<Lectern> getTileClass() {
        return Lectern.class;
    }

    @Override
    public boolean isBlank(Lectern tileState) {
        return tileState.getSnapshotInventory().isEmpty();
    }


    public class LecturnCache {
        public int page;
        public ItemStack book;
        public byte[] persistentData;

        @Override
        public String toString() {
            return "LecturnCache{" +
                    "page=" + page +
                    ", book=" + book +
                    '}';
        }
    }
}
