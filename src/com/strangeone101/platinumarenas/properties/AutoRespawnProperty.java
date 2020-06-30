package com.strangeone101.platinumarenas.properties;

import com.strangeone101.platinumarenas.ArenaProperty;

public class AutoRespawnProperty extends ArenaProperty.BooleanProperty {

    @Override
    public Boolean getDefault() {
        return false;
    }

    @Override
    public String getName() {
        return "autorespawn";
    }

}
