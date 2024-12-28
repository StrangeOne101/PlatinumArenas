package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.BorderCommand;
import com.strangeone101.platinumarenas.commands.DebugCommand;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ArenaListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (BorderCommand.borders.containsKey(event.getPlayer())) {
            if (event.getTo().getWorld() != event.getFrom().getWorld()) {
                BorderCommand.removePlayer(BorderCommand.borders.get(event.getPlayer()), event.getPlayer());
                return;
            }
            Arena arena = BorderCommand.borders.get(event.getPlayer());
            Location min = arena.getCorner1().clone().add((arena.getWidth() / 15 + 10), -10, -(arena.getLength() / 15 + 10));
            Location max = arena.getCorner1().clone().add((arena.getWidth() / 15 + 10), 20, (arena.getLength() / 15 + 10));

            if (!Util.isLocationWithin(min, max, event.getPlayer().getLocation())) {
                BorderCommand.removePlayer(BorderCommand.borders.get(event.getPlayer()), event.getPlayer());
                return;
            }
        }
    }

    @EventHandler
    public void onHit(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && DebugCommand.BLOCK_LISTENERS.containsKey(event.getPlayer().getUniqueId())) {
            DebugCommand.BLOCK_LISTENERS.get(event.getPlayer().getUniqueId()).accept(event.getClickedBlock());
            DebugCommand.BLOCK_LISTENERS.remove(event.getPlayer().getUniqueId());
            event.setCancelled(true);
        }
    }
}
