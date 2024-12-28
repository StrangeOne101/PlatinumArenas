package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

public abstract class LootableContainerWrapper<C extends Container, I extends LootableContainerWrapper.InternalContainer> extends ContainerWrapper<C, I> {

    @Override
    public byte[] write(I cache) {
        byte[] container = super.write(cache);
        SmartWriter out = new SmartWriter();
        out.writeString(cache.lootTable);
        out.writeByteArray(container);

        return out.toByteArray();
    }

    @Override
    public I read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        String lootTable = buffer.getString();
        I container = super.read(buffer.getByteArray());
        container.lootTable = lootTable;
        return container;
    }

    @Override
    public I cache(C baseTileState) {
        I container = super.cache(baseTileState);
        LootTable table = ((Lootable) baseTileState).getLootTable();
        container.lootTable = table == null ? "" : table.getKey().asString();
        return container;
    }

    @Override
    public C place(C baseTileState, I cache) {
        baseTileState = super.place(baseTileState, cache);
        ((Lootable)baseTileState).setLootTable(cache.lootTable.isEmpty() ? null : Bukkit.getLootTable(NamespacedKey.fromString(cache.lootTable)));
        return baseTileState;
    }


    public class InternalContainer extends ContainerWrapper.InternalContainer {
        public String lootTable = "";

        @Override
        public String toString() {
            return "ContainerData{" +
                    "lootTable='" + lootTable + '\'' +
                    ", items=" + items +
                    ", lock='" + lock + '\'' +
                    ", customName='" + customName + '\'' +
                    '}';
        }
    }
}
