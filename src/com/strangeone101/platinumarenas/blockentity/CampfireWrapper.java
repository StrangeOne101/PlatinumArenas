package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Campfire;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CampfireWrapper implements Wrapper<Campfire, CampfireWrapper.InternalContainer> {

    @Override
    public byte[] write(InternalContainer cache) {
        SmartWriter out = new SmartWriter();

        out.writeByte(cache.cookTime.size());
        for (Map.Entry<Byte, Integer> entry : cache.cookTime.entrySet()) {
            out.writeByte(entry.getKey());
            out.writeShort(entry.getValue());
            out.writeShort(cache.totalCookTime.get(entry.getKey()));
            out.writeByteArray(cache.items.get(entry.getKey()).serializeAsBytes());
        }

        return out.toByteArray();
    }

    @Override
    public InternalContainer read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        InternalContainer container = new InternalContainer();

        byte mapLength = buffer.get();
        for (int i = 0; i < mapLength; i++) {
            byte slot = buffer.get();
            short cookTime = buffer.getShort();
            short totalCookTime = buffer.getShort();
            container.cookTime.put(slot, (int) cookTime);
            container.totalCookTime.put(slot, (int) totalCookTime);
            container.items.put(slot, ItemStack.deserializeBytes(buffer.getByteArray()));
        }
        return container;
    }

    @Override
    public InternalContainer cache(Campfire baseTileState) {
        InternalContainer container = new InternalContainer();
        for (byte i = 0; i < baseTileState.getSize(); i++) {
            ItemStack stack = baseTileState.getItem(i);

            if (stack == null) continue;

            container.cookTime.put(i, baseTileState.getCookTime(i));
            container.totalCookTime.put(i, baseTileState.getCookTimeTotal(i));
            container.items.put(i, stack);
        }
        return container;
    }

    @Override
    public Campfire place(Campfire baseTileState, InternalContainer cache) {
        for (Map.Entry<Byte, Integer> entry : cache.cookTime.entrySet()) {
            baseTileState.setCookTime(entry.getKey(), entry.getValue());
            baseTileState.setCookTimeTotal(entry.getKey(), cache.totalCookTime.get(entry.getKey()));
            baseTileState.setItem(entry.getKey(), cache.items.get(entry.getKey()));
        }
        return baseTileState;
    }

    @Override
    public Class<Campfire> getTileClass() {
        return Campfire.class;
    }

    @Override
    public boolean isBlank(Campfire tileState) {
        return tileState.getSize() == 0;
    }

    public class InternalContainer {
        public Map<Byte, Integer> cookTime = new HashMap<>();
        public Map<Byte, Integer> totalCookTime = new HashMap<>();
        public Map<Byte, ItemStack> items = new HashMap<>();

        @Override
        public String toString() {
            return "InternalContainer{" +
                    ", cookTime=" + cookTime +
                    ", totalCookTime=" + totalCookTime +
                    ", items=" + items +
                    '}';
        }
    }
}
