package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.Beacon;
import org.bukkit.potion.PotionEffectType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BeaconWrapper implements Wrapper<Beacon, BeaconWrapper.InternalBeacon> {

    @Override
    public byte[] write(InternalBeacon cache) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        byte[] effect1 = cache.first.getName().getBytes(StandardCharsets.US_ASCII);
        byte[] effect2 = cache.second == null ? new byte[0] : cache.first.getName().getBytes(StandardCharsets.US_ASCII);

        out.write((byte)effect1.length);
        out.write(effect1);
        out.write((byte)effect2.length);
        if (effect2.length > 0) out.write(effect2);

        return out.toByteArray();
    }

    @Override
    public InternalBeacon cache(Beacon baseTileState) {
        InternalBeacon cache = new InternalBeacon();
        cache.first = baseTileState.getPrimaryEffect() == null ? null :baseTileState.getPrimaryEffect().getType();
        cache.second = baseTileState.getSecondaryEffect() == null ? null : baseTileState.getSecondaryEffect().getType();

        if (cache.second == null && baseTileState.getPrimaryEffect().getAmplifier() > 0) {
            cache.second = cache.first;
        }
        return cache;
    }

    @Override
    public InternalBeacon read(byte[] bytes) {
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.put(bytes);
        buff.position(0);

        int length = buff.get();
        byte[] effect1 = new byte[length];
        for (int i = 0; i < length; i++) effect1[i] = buff.get();
        String effect1S = new String(effect1, StandardCharsets.US_ASCII);

        int length2 = buff.get();
        byte[] effect2 = new byte[length2];
        for (int i = 0; i < length2; i++) effect2[i] = buff.get();
        String effect2S = new String(effect2, StandardCharsets.US_ASCII);

        InternalBeacon cache = new InternalBeacon();
        cache.first = PotionEffectType.getByName(effect1S);
        cache.second = PotionEffectType.getByName(effect2S);

        return cache;
    }

    @Override
    public Beacon place(Beacon baseTileState, InternalBeacon cache) {
        baseTileState.setPrimaryEffect(cache.first);
        baseTileState.setSecondaryEffect(cache.second);
        return baseTileState;
    }

    @Override
    public Class<Beacon> getTileClass() {
        return Beacon.class;
    }

    @Override
    public boolean isBlank(Beacon tileState) {
        return tileState.getPrimaryEffect() == null && tileState.getSecondaryEffect() == null;
    }

    public static class InternalBeacon {
        PotionEffectType first, second;
    }
}
