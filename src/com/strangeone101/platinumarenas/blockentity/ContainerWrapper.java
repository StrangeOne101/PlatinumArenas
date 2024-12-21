package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ContainerWrapper<C extends Container, I extends ContainerWrapper.InternalContainer> implements Wrapper<C, I> {

    @Override
    public byte[] write(I cache) {
        SmartWriter out = new SmartWriter();

        out.writeString(cache.lock);

        //Write the custom name
        out.writeString(cache.customName);

        PlatinumArenas.debug("Writing " + cache.items.size() + " items");
        out.writeByte(cache.items.size());

        for (Map.Entry<Byte, ItemStack> entry : (Set<Map.Entry<Byte, ItemStack>>) cache.items.entrySet()) {
            out.writeByte(entry.getKey());
            PlatinumArenas.debug("Writing slot " + entry.getKey());
            byte[] itemBytes = entry.getValue().serializeAsBytes();
            PlatinumArenas.debug("Writing " + itemBytes.length + " bytes");
            out.writeByteArray(itemBytes);
        }
        out.writeByteArray(cache.persistentData);

        return out.toByteArray();
    }

    @Override
    public I read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);
        I container = create();

        container.lock = buffer.getString();

        container.customName = buffer.getString();

        byte mapLength = buffer.get();
        PlatinumArenas.debug("Reading " + mapLength + " items");
        for (int i = 0; i < mapLength; i++) {
            byte slot = buffer.get();
            ItemStack stack = ItemStack.deserializeBytes(buffer.getByteArray());
            container.items.put(slot, stack);
            PlatinumArenas.INSTANCE.getLogger().info("Loaded " + stack + " on slot " + slot);
        }
        container.persistentData = buffer.getByteArray();

        return container;
    }

    @Override
    public I cache(C baseTileState) {
        I container = create();
        container.lock = baseTileState.getLock();
        container.customName = baseTileState.getCustomName();
        container.items = new HashMap<>();
        int max = Math.min(127, baseTileState.getSnapshotInventory().getContents().length); //Just in case some mod made over 128 items in one container

        PlatinumArenas.debug("" + max + " items being cached");
        for (byte i = 0; i < max; i++) {
            ItemStack stack = baseTileState.getSnapshotInventory().getContents()[i];
            if (stack == null || stack.getType().isEmpty()) continue;
            PlatinumArenas.debug("item is " + stack.toString() + " in slot " + i);
            container.items.put(i, stack.clone());
        }
        try {
            container.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return container;
    }

    @Override
    public C place(C baseTileState, I cache) {
        baseTileState.setLock(cache.lock);
        baseTileState.setCustomName(cache.customName);
        ItemStack[] contents = new ItemStack[baseTileState.getSnapshotInventory().getContents().length];
        PlatinumArenas.debug("placing " + cache.items.size());
        for (Map.Entry<Byte, ItemStack> entry : (Set<Map.Entry<Byte, ItemStack>>) cache.items.entrySet()) {
            contents[entry.getKey()] = entry.getValue().clone();
            PlatinumArenas.debug("item placed is " + entry.getValue().toString() + " in slot " + entry.getKey());
        }
        baseTileState.getSnapshotInventory().setContents(contents);
        try {
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baseTileState;
    }

    public class InternalContainer {
        public Map<Byte, ItemStack> items = new HashMap<>();
        public String lock;
        public String customName;
        public byte[] persistentData;
    }

    public abstract I create();
}
