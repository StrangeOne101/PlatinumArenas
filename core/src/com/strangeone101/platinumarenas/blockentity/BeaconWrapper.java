package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Beacon;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class BeaconWrapper implements Wrapper<Beacon, BeaconWrapper.InternalBeacon> {

    @Override
    public byte[] write(InternalBeacon cache) {
        SmartWriter out = new SmartWriter();
        String effect1 = cache.first.getKey().asString();
        String effect2 = cache.second == null ? "" : cache.second.getKey().asString();

        out.writeString(effect1);
        out.writeString(effect2);

        out.writeByteArray(cache.persistentData);

        return out.toByteArray();
    }

    @Override
    public InternalBeacon cache(Beacon baseTileState) {
        InternalBeacon cache = new InternalBeacon();
        cache.first = baseTileState.getPrimaryEffect() == null ? null : baseTileState.getPrimaryEffect().getType();
        cache.second = baseTileState.getSecondaryEffect() == null ? null : baseTileState.getSecondaryEffect().getType();

        if (cache.second == null && baseTileState.getPrimaryEffect().getAmplifier() > 0) {
            cache.second = cache.first;
        }
        try {
            cache.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cache;
    }

    @Override
    public InternalBeacon read(byte[] bytes) {
        SmartReader buff = new SmartReader(bytes);

        String effect1 = buff.getString();
        String effect2 = buff.getString();

        InternalBeacon cache = new InternalBeacon();
        cache.first = effect1.isEmpty() ? null : PotionEffectType.getByName(effect1);
        cache.second = effect2.isEmpty() ? null : PotionEffectType.getByName(effect2);
        cache.persistentData = buff.getByteArray();

        return cache;
    }

    @Override
    public Beacon place(Beacon baseTileState, InternalBeacon cache) {
        baseTileState.setPrimaryEffect(cache.first);
        baseTileState.setSecondaryEffect(cache.second);
        try {
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseTileState;
    }

    @Override
    public Class<Beacon> getTileClass() {
        return Beacon.class;
    }

    @Override
    public boolean isBlank(Beacon tileState) {
        return tileState.getPrimaryEffect() == null && tileState.getSecondaryEffect() == null && tileState.getPersistentDataContainer().isEmpty();
    }

    public static class InternalBeacon {
        PotionEffectType first, second;
        byte[] persistentData;

        @Override
        public String toString() {
            return "BeaconData{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }
}
