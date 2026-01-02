package com.github.tejaslamba2006.identicalhunters.managers;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages hunters and target for the manhunt game
 */
public class HunterManager {

    private final IdenticalHunters plugin;
    private final Set<UUID> hunters;
    private UUID target;
    private boolean gameActive;

    public HunterManager(IdenticalHunters plugin) {
        this.plugin = plugin;
        this.hunters = new HashSet<>();
        this.target = null;
        this.gameActive = false;
    }

    /**
     * Add a player as a hunter
     */
    public boolean addHunter(UUID playerId) {
        if (hunters.contains(playerId)) {
            return false;
        }

        hunters.add(playerId);

        // If game is active, apply disguise immediately
        if (gameActive) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                plugin.getDisguiseManager().applyDisguise(player);
            }
        }

        return true;
    }

    /**
     * Remove a player from hunters
     */
    public boolean removeHunter(UUID playerId) {
        if (!hunters.contains(playerId)) {
            return false;
        }

        hunters.remove(playerId);

        // Remove disguise
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            plugin.getDisguiseManager().removeDisguise(player);
        }

        return true;
    }

    /**
     * Check if a player is a hunter
     */
    public boolean isHunter(UUID playerId) {
        return hunters.contains(playerId);
    }

    /**
     * Check if a player is a hunter
     */
    public boolean isHunter(Player player) {
        return isHunter(player.getUniqueId());
    }

    /**
     * Get all hunter UUIDs
     */
    public Set<UUID> getHunters() {
        return new HashSet<>(hunters);
    }

    /**
     * Get number of hunters
     */
    public int getHunterCount() {
        return hunters.size();
    }

    /**
     * Set the target (speedrunner)
     */
    public void setTarget(UUID playerId) {
        this.target = playerId;
    }

    /**
     * Remove the target
     */
    public void removeTarget() {
        this.target = null;
    }

    /**
     * Get the target UUID
     */
    public UUID getTarget() {
        return target;
    }

    /**
     * Get the target player
     */
    public Player getTargetPlayer() {
        if (target == null)
            return null;
        return Bukkit.getPlayer(target);
    }

    /**
     * Check if a player is the target
     */
    public boolean isTarget(UUID playerId) {
        return target != null && target.equals(playerId);
    }

    /**
     * Check if a player is the target
     */
    public boolean isTarget(Player player) {
        return isTarget(player.getUniqueId());
    }

    /**
     * Start the game - apply all disguises
     */
    public boolean startGame() {
        int minHunters = plugin.getConfig().getInt("game.min-hunters", 1);

        if (hunters.size() < minHunters) {
            return false;
        }

        gameActive = true;

        if (plugin.getConfig().getBoolean("game.auto-apply-on-start", true)) {
            for (UUID hunterId : hunters) {
                Player hunter = Bukkit.getPlayer(hunterId);
                if (hunter != null && hunter.isOnline()) {
                    plugin.getDisguiseManager().applyDisguise(hunter);
                }
            }
        }

        // Broadcast
        if (plugin.getConfig().getBoolean("game.broadcast-events", true)) {
            Bukkit.broadcast(plugin.getMessage("game-started"));
        }

        return true;
    }

    /**
     * Stop the game - remove all disguises
     */
    public void stopGame() {
        gameActive = false;

        if (plugin.getConfig().getBoolean("game.auto-remove-on-end", true)) {
            plugin.getDisguiseManager().removeAllDisguises();
        }

        // Broadcast
        if (plugin.getConfig().getBoolean("game.broadcast-events", true)) {
            Bukkit.broadcast(plugin.getMessage("game-stopped"));
        }
    }

    /**
     * Check if game is active
     */
    public boolean isGameActive() {
        return gameActive;
    }

    /**
     * Clear all data
     */
    public void clearAll() {
        stopGame();
        hunters.clear();
        target = null;
    }

    /**
     * Reload manager
     */
    public void reload() {
        // Nothing specific to reload, config values are read dynamically
    }
}
