package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.Furnace;

import java.nio.ByteBuffer;

public class FurnaceWrapper extends ContainerWrapper<Furnace, FurnaceWrapper.InternalContainer> {

    @Override
    public byte[] write(InternalContainer cache) {
        byte[] container = super.write(cache);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeShort(cache.cookTime);
        out.writeShort(cache.burnTime);
        out.writeDouble(cache.multiplier);
        out.write(container);

        return container;
    }

    @Override
    public InternalContainer read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);

        short cookTime = buffer.getShort();
        short burnTime = buffer.getShort();
        double multiplier = buffer.getDouble();

        byte[] containerBytes = new byte[bytes.length - (2 + 2 + 8)];
        buffer.get(containerBytes);

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
