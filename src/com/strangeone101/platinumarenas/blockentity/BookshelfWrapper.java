package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BookshelfWrapper implements Wrapper<ChiseledBookshelf, BookshelfWrapper.BookshelfCache> {

    public class BookshelfCache {
        public int lastInteracted;
        public Map<Byte, ItemStack> books = new HashMap<>();

        @Override
        public String toString() {
            return "BookshelfCache{" +
                    "lastInteracted=" + lastInteracted +
                    ", books=" + books +
                    '}';
        }
    }

    public byte[] write(BookshelfCache cache) {
        SmartWriter out = new SmartWriter();

        out.writeInt(cache.lastInteracted);
        out.writeInt(cache.books.size());

        for (Map.Entry<Byte, ItemStack> entry : cache.books.entrySet()) {
            out.writeByte(entry.getKey());
            byte[] itemBytes = entry.getValue().serializeAsBytes();
            out.writeByteArray(itemBytes);
        }

        return out.toByteArray();
    }

    public BookshelfCache read(byte[] bytes) {
        BookshelfCache cache = new BookshelfCache();

        SmartReader buffer = new SmartReader(bytes);

        cache.lastInteracted = buffer.getInt();

        int mapLength = buffer.getInt();
        for (int i = 0; i < mapLength; i++) {
            byte slot = buffer.get();
            byte[] itemBytes = buffer.getByteArray();

            cache.books.put(slot, ItemStack.deserializeBytes(itemBytes));
        }

        return cache;
    }

    public BookshelfCache cache(ChiseledBookshelf baseTileState) {
        BookshelfCache cache = new BookshelfCache();

        cache.lastInteracted = baseTileState.getLastInteractedSlot();

        for (byte i = 0; i < baseTileState.getSnapshotInventory().getSize(); i++) {
            ItemStack stack = baseTileState.getSnapshotInventory().getItem(i);
            if (stack == null) continue;

            cache.books.put(i, stack);
        }

        return cache;
    }

    public ChiseledBookshelf place(ChiseledBookshelf baseTileState, BookshelfCache cache) {
        baseTileState.setLastInteractedSlot(cache.lastInteracted);

        for (Map.Entry<Byte, ItemStack> entry : cache.books.entrySet()) {
            baseTileState.getSnapshotInventory().setItem(entry.getKey(), entry.getValue());
        }

        return baseTileState;
    }

    @Override
    public Class<ChiseledBookshelf> getTileClass() {
        return ChiseledBookshelf.class;
    }

    @Override
    public boolean isBlank(ChiseledBookshelf tileState) {
        return tileState.getInventory().isEmpty();
    }
}
