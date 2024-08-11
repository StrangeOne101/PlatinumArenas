package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BookshelfWrapper implements Wrapper<ChiseledBookshelf, BookshelfWrapper.BookshelfCache> {

    public class BookshelfCache {
        public int lastInteracted;
        public Map<Integer, ItemStack> books = new HashMap<>();

        @Override
        public String toString() {
            return "BookshelfCache{" +
                    "lastInteracted=" + lastInteracted +
                    ", books=" + books +
                    '}';
        }
    }

    public byte[] write(BookshelfCache cache) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeInt(cache.lastInteracted);
        out.writeInt(cache.books.size());

        for (Map.Entry<Integer, ItemStack> entry : cache.books.entrySet()) {
            out.writeInt(entry.getKey());
            byte[] itemBytes = entry.getValue().serializeAsBytes();
            out.writeInt(itemBytes.length);
            out.write(itemBytes);
        }

        return out.toByteArray();
    }

    public BookshelfCache read(byte[] bytes) {
        BookshelfCache cache = new BookshelfCache();

        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);

        cache.lastInteracted = buffer.getInt();

        int mapLength = buffer.getInt();
        for (int i = 0; i < mapLength; i++) {
            int slot = buffer.getInt();
            int itemLength = buffer.getInt();
            byte[] itemBytes = new byte[itemLength];
            buffer.get(itemBytes);
            cache.books.put(slot, ItemStack.deserializeBytes(itemBytes));
        }

        return cache;
    }

    public BookshelfCache cache(ChiseledBookshelf baseTileState) {
        BookshelfCache cache = new BookshelfCache();

        cache.lastInteracted = baseTileState.getLastInteractedSlot();

        for (int i = 0; i < baseTileState.getInventory().getSize(); i++) {
            cache.books.put(i, baseTileState.getInventory().getItem(i));
        }

        return cache;
    }

    public ChiseledBookshelf place(ChiseledBookshelf baseTileState, BookshelfCache cache) {
        baseTileState.setLastInteractedSlot(cache.lastInteracted);

        for (Map.Entry<Integer, ItemStack> entry : cache.books.entrySet()) {
            baseTileState.getInventory().setItem(entry.getKey(), entry.getValue());
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
