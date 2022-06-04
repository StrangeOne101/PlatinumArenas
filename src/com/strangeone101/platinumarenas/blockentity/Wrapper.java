package com.strangeone101.platinumarenas.blockentity;

import org.bukkit.block.TileState;

public interface Wrapper<T extends TileState, S> {

    byte[] write(S cache);

    S cache(T baseTileState);

    S read(byte[] bytes);

    T place(T baseTileState, S cache);

    Class<T> getTileClass();

    boolean isBlank(T tileState);

    default T cast(TileState state) {
        return (T) state;
    }
}
