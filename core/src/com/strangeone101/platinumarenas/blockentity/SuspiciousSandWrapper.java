package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrushableBlock;
import org.bukkit.inventory.ItemStack;

public class SuspiciousSandWrapper implements Wrapper<BrushableBlock, SuspiciousSandWrapper.SandData> {

    @Override
    public byte[] write(SandData cache) {
        SmartWriter out = new SmartWriter();
        if (cache.stack == null || cache.stack.isEmpty()) out.writeByteArray(new byte[0]);
        else out.writeByteArray(cache.stack.serializeAsBytes());
        out.writeString(cache.lootTable);
        return out.toByteArray();
    }

    @Override
    public SandData cache(BrushableBlock baseTileState) {
        SandData data = new SandData();
        data.stack = baseTileState.getItem();
        data.lootTable = baseTileState.getLootTable() == null ? null : baseTileState.getLootTable().getKey().toString();
        return data;
    }

    @Override
    public SandData read(byte[] bytes) {
        SmartReader in = new SmartReader(bytes);
        SandData data = new SandData();
        data.stack = ItemStack.deserializeBytes(in.getByteArray());
        data.lootTable = in.getString();
        return data;
    }

    @Override
    public BrushableBlock place(BrushableBlock baseTileState, SandData cache) {
        if (cache.lootTable != null) {
            baseTileState.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(cache.lootTable)));
        } else {
            baseTileState.setItem(cache.stack);
        }

        return baseTileState;
    }

    @Override
    public Class<BrushableBlock> getTileClass() {
        return BrushableBlock.class;
    }

    @Override
    public boolean isBlank(BrushableBlock tileState) {
        return false;
    }

    public static class SandData {
        private ItemStack stack;
        private String lootTable;
    }
}
