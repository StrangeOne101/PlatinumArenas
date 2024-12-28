package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Furnace;

public class FurnaceWrapper extends ContainerWrapper<Furnace, FurnaceWrapper.InternalContainer> {

    @Override
    public byte[] write(InternalContainer cache) {
        byte[] container = super.write(cache);
        SmartWriter out = new SmartWriter();
        out.writeShort(cache.cookTime);
        out.writeShort(cache.burnTime);
        out.writeDouble(cache.multiplier);
        out.writeByteArray(container);

        return out.toByteArray();
    }

    @Override
    public InternalContainer read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        short cookTime = buffer.getShort();
        short burnTime = buffer.getShort();
        double multiplier = buffer.getDouble();

        byte[] containerBytes = buffer.getByteArray();

        InternalContainer container = super.read(containerBytes);
        container.burnTime = burnTime;
        container.cookTime = cookTime;
        container.multiplier = multiplier;
        return container;
    }

    @Override
    public InternalContainer cache(Furnace baseTileState) {
        InternalContainer container = super.cache(baseTileState);
        container.cookTime = baseTileState.getCookTime();
        container.burnTime = baseTileState.getBurnTime();
        container.multiplier = baseTileState.getCookSpeedMultiplier();
        return container;
    }

    @Override
    public Furnace place(Furnace baseTileState, InternalContainer cache) {
        baseTileState = super.place(baseTileState, cache);
        baseTileState.setBurnTime(cache.burnTime);
        baseTileState.setCookTime(cache.cookTime);
        baseTileState.setCookSpeedMultiplier(cache.multiplier);
        return baseTileState;
    }

    @Override
    public InternalContainer create() {
        return new InternalContainer();
    }

    @Override
    public Class<Furnace> getTileClass() {
        return Furnace.class;
    }

    @Override
    public boolean isBlank(Furnace tileState) {
        return tileState.getBurnTime() == 0 && tileState.getCookTime() == 0 && tileState.getInventory().isEmpty();
    }

    public class InternalContainer extends ContainerWrapper.InternalContainer {
        public double multiplier;
        public short cookTime;
        public short burnTime;

        @Override
        public String toString() {
            return "InternalContainer{" +
                    "multiplier=" + multiplier +
                    ", cookTime=" + cookTime +
                    ", burnTime=" + burnTime +
                    ", items=" + items +
                    ", lock='" + lock + '\'' +
                    ", customName='" + customName + '\'' +
                    '}';
        }
    }
}
