package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.nio.ByteBuffer;

public class ChestWrapper extends ContainerWrapper<Container, ChestWrapper.InternalContainer> {

    @Override
    public byte[] write(InternalContainer cache) {
        byte[] container = super.write(cache);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        byte[] lootTable = cache.table == null ? new byte[0] : cache.table.getKey().toString().getBytes();
        out.writeInt(lootTable.length);
        out.write(lootTable);
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

        byte[] containerBytes = new byte[bytes.length - (loottableLength + 4)];
        buffer.get(containerBytes);

        InternalContainer container = super.read(containerBytes);
        container.table = loottableString.isEmpty() ? null : Bukkit.getLootTable(NamespacedKey.fromString(loottableString));
        return container;
    }

    @Override
    public InternalContainer cache(Container baseTileState) {
        InternalContainer container = super.cache(baseTileState);
        container.table = ((Lootable)baseTileState).getLootTable();
        return container;
    }

    @Override
    public Container place(Container baseTileState, InternalContainer cache) {
        baseTileState = super.place(baseTileState, cache);
        ((Lootable)baseTileState).setLootTable(cache.table);
        return baseTileState;
    }

    @Override
    public InternalContainer create() {
        return new InternalContainer();
    }

    @Override
    public Class<Container> getTileClass() {
        return Container.class;
    }

    @Override
    public boolean isBlank(Container tileState) {
        return !((Lootable)tileState).hasLootTable() && tileState.getInventory().isEmpty();
    }


    public class InternalContainer extends ContainerWrapper.InternalContainer {
        public LootTable table;

        @Override
        public String toString() {
            return "InternalContainer{" +
                    "table=" + table +
                    ", items=" + items +
                    ", lock='" + lock + '\'' +
                    ", customName='" + customName + '\'' +
                    '}';
        }
    }
}
