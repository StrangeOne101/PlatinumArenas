package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.nio.ByteBuffer;

public class ChestWrapper extends LootableContainerWrapper<Container, LootableContainerWrapper.InternalContainer> {

    @Override
    public byte[] write(LootableContainerWrapper.InternalContainer cache) {
        return super.write(cache);
    }

    @Override
    public LootableContainerWrapper.InternalContainer read(byte[] bytes) {
        return super.read(bytes);
    }

    @Override
    public LootableContainerWrapper.InternalContainer cache(Container baseTileState) {
        return super.cache(baseTileState);
    }

    @Override
    public Container place(Container baseTileState, LootableContainerWrapper.InternalContainer cache) {
        return super.place(baseTileState, cache);
    }

    @Override
    public LootableContainerWrapper.InternalContainer create() {
        return new LootableContainerWrapper.InternalContainer();
    }

    @Override
    public Class<Container> getTileClass() {
        return Container.class;
    }

    @Override
    public boolean isBlank(Container tileState) {
        return !((Lootable)tileState).hasLootTable() && tileState.getInventory().isEmpty();
    }
}
