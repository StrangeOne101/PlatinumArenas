package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.Util;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.ItemStack;

public class PotWrapper implements Wrapper<DecoratedPot, PotWrapper.InternalPot> {
    @Override
    public byte[] write(PotWrapper.InternalPot cache) {
        SmartWriter out = new SmartWriter();
        out.writeString(cache.left.getKey().toString()); //Left
        out.writeString(cache.right.getKey().toString()); //Right
        out.writeString(cache.front.getKey().toString()); //Front
        out.writeString(cache.back.getKey().toString()); //Back

        if (Util.isPaperSupported() && cache.item != null) {
            out.writeByte(1);
            out.writeByteArray(cache.item.serializeAsBytes());
        } else {
            out.writeByte(0);
        }

        return out.toByteArray();
    }

    @Override
    public PotWrapper.InternalPot cache(DecoratedPot baseTileState) {
        InternalPot pot = new InternalPot();
        pot.left = baseTileState.getSherd(DecoratedPot.Side.LEFT);
        pot.right = baseTileState.getSherd(DecoratedPot.Side.RIGHT);
        pot.front = baseTileState.getSherd(DecoratedPot.Side.FRONT);
        pot.back = baseTileState.getSherd(DecoratedPot.Side.BACK);
        pot.item = baseTileState.getSnapshotInventory().getItem();

        return pot;
    }

    @Override
    public PotWrapper.InternalPot read(byte[] bytes) {
        SmartReader in = new SmartReader(bytes);
        InternalPot pot = new InternalPot();
        pot.left = Material.matchMaterial(in.getString());
        pot.right = Material.matchMaterial(in.getString());
        pot.front = Material.matchMaterial(in.getString());
        pot.back = Material.matchMaterial(in.getString());

        if (in.get() == 1) {
            pot.item = ItemStack.deserializeBytes(in.getByteArray());
        }

        return pot;
    }

    @Override
    public DecoratedPot place(DecoratedPot baseTileState, PotWrapper.InternalPot cache) {
        baseTileState.setSherd(DecoratedPot.Side.LEFT, cache.left);
        baseTileState.setSherd(DecoratedPot.Side.RIGHT, cache.right);
        baseTileState.setSherd(DecoratedPot.Side.FRONT, cache.front);
        baseTileState.setSherd(DecoratedPot.Side.BACK, cache.back);
        baseTileState.getInventory().setItem(cache.item);
        return baseTileState;
    }

    @Override
    public Class<DecoratedPot> getTileClass() {
        return DecoratedPot.class;
    }

    @Override
    public boolean isBlank(DecoratedPot tileState) {
        return tileState.getSherds().values().stream().allMatch(m -> m == Material.BRICK) && tileState.getInventory().getItem() == null;
    }

    public static class InternalPot {
        private Material left;
        private Material right;
        private Material front;
        private Material back;
        private ItemStack item;

        @Override
        public String toString() {
            return "InternalPot{" +
                    "left=" + left +
                    ", right=" + right +
                    ", front=" + front +
                    ", back=" + back +
                    ", item=" + item +
                    '}';
        }
    }
}
