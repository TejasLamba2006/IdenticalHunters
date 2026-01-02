package com.github.tejaslamba2006.identicalhunters.listeners;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class HunterListener implements Listener {

    private final IdenticalHunters plugin;

    public HunterListener(IdenticalHunters plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getHunterManager().isGameActive() && plugin.getHunterManager().isHunter(player)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getDisguiseManager().applyDisguise(player);
            }, 20L); // 1 second delay
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getDisguiseManager().isDisguised(player)) {
            plugin.getDisguiseManager().removeDisguise(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }

        if (plugin.getConfig().getBoolean("game.remove-on-death", false)) {
            plugin.getDisguiseManager().removeDisguise(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getHunterManager().isGameActive()) {
            return;
        }

        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }

        if (plugin.getConfig().getBoolean("game.reapply-on-respawn", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getDisguiseManager().applyDisguise(player);
            }, 5L);
        }
    }
}
