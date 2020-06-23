package com.strangeone101.platinumarenas.properties;

import com.strangeone101.platinumarenas.ArenaProperty;
import org.bukkit.command.CommandSender;

public class RespawnTimerProperty extends ArenaProperty<Integer> {

    @Override
    public Integer getDefault() {
        return 3;
    }

    @Override
    public String getName() {
        return "respawntimer";
    }

    @Override
    public void load(String data) {
        this.set(Integer.valueOf(data));
    }

    @Override
    public String save() {
        return get().toString();
    }

    @Override
    public String[] values() {
        return new String[0];
    }

    @Override
    public Integer parse(CommandSender sender, String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {}
        return null;
    }
}
