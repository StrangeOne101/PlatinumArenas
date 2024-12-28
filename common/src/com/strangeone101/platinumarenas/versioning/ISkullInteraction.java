package com.strangeone101.platinumarenas.versioning;

import org.bukkit.block.Skull;

import java.util.UUID;

public interface ISkullInteraction<T> {

    T cache(Skull baseTileState);

    Skull set(Skull baseTileState, T data);

    String getName(T cache);

    UUID getUUID(T cache);

    String getTexture(T cache);

    T create(UUID uuid, String texture);

    boolean isEnabled();

}
