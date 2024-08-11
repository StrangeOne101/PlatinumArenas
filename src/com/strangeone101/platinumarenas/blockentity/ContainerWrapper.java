package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ContainerWrapper<C extends Container, I extends ContainerWrapper.InternalContainer> implements Wrapper<C, I> {

    @Override
    public byte[] write(I cache) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        byte[] lockBytes = cache.lock == null ? new byte[0] : cache.lock.getBytes();
        out.writeInt(lockBytes.length);
        out.write(lockBytes);

        //Write the custom name
        out.write(cache.customName == null ? 0 : cache.customName.getBytes().length);
        if (cache.customName != null) out.write(cache.customName.getBytes());


        out.writeInt(cache.items.size());

        for (Map.Entry<Integer, ItemStack> entry : (Set<Map.Entry<Integer, ItemStack>>) cache.items.entrySet()) {
            out.writeInt(entry.getKey());
            byte[] itemBytes = entry.getValue().serializeAsBytes();
            out.writeInt(itemBytes.length);
            out.write(itemBytes);
        }

        return out.toByteArray();
    }

    @Override
    public I read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        I container = create();

        int lockLength = buffer.getInt(); //Get the lock string
        if (lockLength > 0) {
            byte[] lockBytes = new byte[lockLength];
            buffer.get(lockBytes);
            container.lock = new String(lockBytes);
        }

        int length = buffer.getInt();
        if (length > 0) {
            byte[] nameBytes = new byte[length];
            buffer.get(nameBytes);
            container.customName = new String(nameBytes);
        }

        int mapLength = buffer.getInt();
        for (int i = 0; i < mapLength; i++) {
            int slot = buffer.getInt();
            int itemLength = buffer.getInt();
            byte[] itemBytes = new byte[itemLength];
            buffer.get(itemBytes);
            container.items.put(slot, ItemStack.deserializeBytes(itemBytes));
        }

        return container;
    }

    @Override
    public I cache(C baseTileState) {
        I container = create();
        container.lock = baseTileState.getLock();
        container.customName = baseTileState.getCustomName();
        container.items = new HashMap<>();
        for (int i = 0; i < baseTileState.getInventory().getContents().length; i++) {
            ItemStack stack = baseTileState.getInventory().getContents()[i];
            if (stack == null || stack.getType() == Material.AIR) continue;
            container.items.put(i, stack);
        }
        return container;
    }

    @Override
    public C place(C baseTileState, I cache) {
        baseTileState.setLock(cache.lock);
        baseTileState.setCustomName(cache.customName);
        ItemStack[] contents = baseTileState.getInventory().getContents();
        for (Map.Entry<Integer, ItemStack> entry : (Set<Map.Entry<Integer, ItemStack>>) cache.items.entrySet()) {
            contents[entry.getKey()] = entry.getValue();
        }
        baseTileState.getInventory().setContents(contents);

        return baseTileState;
    }

    public class InternalContainer {
        public Map<Integer, ItemStack> items;
        public String lock;
        public String customName;
    }

    public abstract I create();
}
