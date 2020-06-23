package com.strangeone101.platinumarenas.properties;

import com.strangeone101.platinumarenas.ArenaProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnLocationProperty extends ArenaProperty<Location> {
    @Override
    public Location getDefault() {
        return null;
    }

    @Override
    public String getName() {
        return "respawnlocation";
    }

    @Override
    public void load(String data) {
        World world = Bukkit.getWorld(data.split(",")[0]);
        double x = Double.valueOf(data.split(",")[1]);
        double y = Double.valueOf(data.split(",")[2]);
        double z = Double.valueOf(data.split(",")[3]);

        this.set(new Location(world, x, y, z));
    }

    @Override
    public String save() {
        return this.get().getWorld().getName() + "," + this.get().getX() + "," + this.get().getY() + "," + this.get().getZ();
    }

    @Override
    public String[] values() {
        return new String[] {"X Y Z WORLD"};
    }

    @Override
    public Location parse(CommandSender sender, String string) {
        if (string.split(" ").length >= 3) {
            try {
                double x = Double.parseDouble(string.split(" ")[0]);
                double y = Double.parseDouble(string.split(" ")[1]);
                double z = Double.parseDouble(string.split(" ")[2]);
                String w = string.split(" ").length > 3 ? string.split(" ")[3] : (!(sender instanceof Player) ? null : ((Player)sender).getWorld().getName());

                if (w != null && Bukkit.getWorld(w) != null) new Location(Bukkit.getWorld(w), x, y, z);
            } catch (NumberFormatException e) {}
        }
        return null;
    }
}
