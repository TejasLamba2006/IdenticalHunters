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

/**
 * Handles hunter-related events
 */
public class HunterListener implements Listener {

    private final IdenticalHunters plugin;

    public HunterListener(IdenticalHunters plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join - re-apply disguise if needed
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if game is active and player is a hunter
        if (plugin.getHunterManager().isGameActive() && plugin.getHunterManager().isHunter(player)) {
            // Re-apply disguise with slight delay to ensure player is fully loaded
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getDisguiseManager().applyDisguise(player);
            }, 20L); // 1 second delay
        }
    }

    /**
     * Handle player quit - clean up disguise data
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove disguise (will save their armor if in stealth mode)
        if (plugin.getDisguiseManager().isDisguised(player)) {
            plugin.getDisguiseManager().removeDisguise(player);
        }
    }

    /**
     * Handle player death
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }

        // Remove disguise on death if configured
        if (plugin.getConfig().getBoolean("game.remove-on-death", false)) {
            plugin.getDisguiseManager().removeDisguise(player);
        }
    }

    /**
     * Handle player respawn - re-apply disguise if needed
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getHunterManager().isGameActive()) {
            return;
        }

        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }

        // Re-apply disguise on respawn if configured
        if (plugin.getConfig().getBoolean("game.reapply-on-respawn", true)) {
            // Slight delay to ensure inventory is ready
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getDisguiseManager().applyDisguise(player);
            }, 5L);
        }
    }
}
