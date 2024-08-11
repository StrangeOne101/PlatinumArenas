package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.BitField;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.Crafter;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class CrafterWrapper extends ContainerWrapper<Crafter, CrafterWrapper.InternalContainer> {

    static Map<Integer, BitField> bitFields = new HashMap<>();

    static {
        bitFields.put(0, new BitField(0b1000000000));
        bitFields.put(1, new BitField(0b0100000000));
        bitFields.put(2, new BitField(0b0010000000));
        bitFields.put(3, new BitField(0b0001000000));
        bitFields.put(4, new BitField(0b0000100000));
        bitFields.put(5, new BitField(0b0000010000));
        bitFields.put(6, new BitField(0b0000001000));
        bitFields.put(7, new BitField(0b0000000100));
        bitFields.put(8, new BitField(0b0000000010));
        bitFields.put(9, new BitField(0b0000000001));
    }

    @Override
    public byte[] write(InternalContainer cache) {
        byte[] container = super.write(cache);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        byte[] lootTable = cache.table == null ? new byte[0] : cache.table.getKey().toString().getBytes();
        out.writeInt(lootTable.length);
        out.write(lootTable);
        out.writeInt(cache.craftingTicks);
        out.writeShort(getLockedSlotsCompressed(cache.triggered, cache.lockedSlots));
        out.write(container);

        return container;
    }

    @Override
    public InternalContainer read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);

        int loottableLength = buffer.getInt();
        byte[] loottableBytes = new byte[loottableLength];
        buffer.get(loottableBytes);
        String loottableString = new String(loottableBytes);

        int craftingTicks = buffer.getInt();
        short lockedSlots = buffer.getShort();

        byte[] containerBytes = new byte[bytes.length - (loottableLength + 4 + 2 + 4)];
        buffer.get(containerBytes);

        InternalContainer container = super.read(containerBytes);
        container.table = loottableString.isEmpty() ? null : Bukkit.getLootTable(NamespacedKey.fromString(loottableString));
        container.craftingTicks = craftingTicks;
        container.lockedSlots = getLockedSlotsFromCompressed(lockedSlots);
        container.triggered = bitFields.get(0).getShortValue(lockedSlots) == 1;
        return container;
    }

    @Override
    public InternalContainer cache(Crafter baseTileState) {
        InternalContainer container = super.cache(baseTileState);
        container.table = ((Lootable)baseTileState).getLootTable();
        container.craftingTicks = baseTileState.getCraftingTicks();
        container.triggered = baseTileState.isTriggered();
        container.lockedSlots = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            container.lockedSlots.put(i, baseTileState.isSlotDisabled(i - 1));
        }
        return container;
    }

    @Override
    public Crafter place(Crafter baseTileState, InternalContainer cache) {
        baseTileState = super.place(baseTileState, cache);
        ((Lootable)baseTileState).setLootTable(cache.table);
        baseTileState.setCraftingTicks(cache.craftingTicks);
        baseTileState.setTriggered(cache.triggered);

        for (int i = 1; i <= 9; i++) {
            baseTileState.setSlotDisabled(i - 1, cache.lockedSlots.get(i));
        }
        return baseTileState;
    }

    @Override
    public InternalContainer create() {
        return new InternalContainer();
    }

    @Override
    public Class<Crafter> getTileClass() {
        return Crafter.class;
    }

    @Override
    public boolean isBlank(Crafter tileState) {
        return !((Lootable)tileState).hasLootTable() && tileState.getInventory().isEmpty();
    }

    public short getLockedSlotsCompressed(boolean triggered, Map<Integer, Boolean> lockedSlots) {
        short output = bitFields.get(0).setShortBoolean((short) 0, triggered);

        for (int i = 1; i <= 9; i++) {
            output = bitFields.get(i).setShortBoolean(output, lockedSlots.get(i));
        }

        return output;
    }

    public Map<Integer, Boolean> getLockedSlotsFromCompressed(short compressed) {
        Map<Integer, Boolean> output = new HashMap<>();

        for (int i = 1; i <= 9; i++) {
            output.put(i, bitFields.get(i).getShortValue(compressed) == 1);
        }

        return output;
    }


    public class InternalContainer extends ContainerWrapper.InternalContainer {
        public LootTable table;
        public int craftingTicks;
        public boolean triggered;
        public Map<Integer, Boolean> lockedSlots = new HashMap<>();

        @Override
        public String toString() {
            return "InternalContainer{" +
                    "table=" + table +
                    ", craftingTicks=" + craftingTicks +
                    ", triggered=" + triggered +
                    ", lockedSlots=" + lockedSlots +
                    ", items=" + items +
                    ", lock='" + lock + '\'' +
                    ", customName='" + customName + '\'' +
                    '}';
        }
    }
}
