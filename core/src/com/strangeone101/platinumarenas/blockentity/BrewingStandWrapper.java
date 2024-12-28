package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.BrewingStand;

import java.nio.ByteBuffer;

public class BrewingStandWrapper extends ContainerWrapper<BrewingStand, BrewingStandWrapper.InternalContainer> {

    @Override
    public byte[] write(InternalContainer cache) {
        byte[] container = super.write(cache);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeInt(cache.brewingTime);
        out.writeInt(cache.fuelLevel);
        out.write(container);

        return out.toByteArray();
    }

    @Override
    public InternalContainer read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(0);

        int brewingTime = buffer.getInt();
        int fuelLevel = buffer.getInt();

        byte[] containerBytes = new byte[bytes.length - 8];
        buffer.get(containerBytes);

        InternalContainer container = super.read(containerBytes);
        container.brewingTime = brewingTime;
        container.fuelLevel = fuelLevel;
        return container;
    }

    @Override
    public InternalContainer cache(BrewingStand baseTileState) {
        InternalContainer container = super.cache(baseTileState);
        container.brewingTime = baseTileState.getBrewingTime();
        container.fuelLevel = baseTileState.getFuelLevel();
        return container;
    }

    @Override
    public BrewingStand place(BrewingStand baseTileState, InternalContainer cache) {
        baseTileState = super.place(baseTileState, cache);
        baseTileState.setBrewingTime(cache.brewingTime);
        baseTileState.setFuelLevel(cache.fuelLevel);
        return baseTileState;
    }

    @Override
    public InternalContainer create() {
        return new InternalContainer();
    }

    @Override
    public Class<BrewingStand> getTileClass() {
        return BrewingStand.class;
    }

    @Override
    public boolean isBlank(BrewingStand tileState) {
        return tileState.getBrewingTime() == 0 && tileState.getFuelLevel() == 0 && tileState.getInventory().isEmpty();
    }


    public class InternalContainer extends ContainerWrapper.InternalContainer {
        public int brewingTime;
        public int fuelLevel;

        @Override
        public String toString() {
            return "InternalContainer{" +
                    "brewingTime=" + brewingTime +
                    ", fuelLevel=" + fuelLevel +
                    ", items=" + items +
                    ", lock='" + lock + '\'' +
                    ", customName='" + customName + '\'' +
                    '}';
        }
    }
}
