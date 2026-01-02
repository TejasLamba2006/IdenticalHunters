package com.github.tejaslamba2006.identicalhunters.managers;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    public boolean addHunter(UUID playerId) {
        if (hunters.contains(playerId)) {
            return false;
        }

        hunters.add(playerId);

        if (gameActive) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                plugin.getDisguiseManager().applyDisguise(player);
            }
        }

        return true;
    }

    public boolean removeHunter(UUID playerId) {
        if (!hunters.contains(playerId)) {
            return false;
        }

        hunters.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            plugin.getDisguiseManager().removeDisguise(player);
        }

        return true;
    }

    public boolean isHunter(UUID playerId) {
        return hunters.contains(playerId);
    }

    public boolean isHunter(Player player) {
        return isHunter(player.getUniqueId());
    }

    public Set<UUID> getHunters() {
        return new HashSet<>(hunters);
    }

    public int getHunterCount() {
        return hunters.size();
    }

    public void setTarget(UUID playerId) {
        this.target = playerId;
    }

    public void removeTarget() {
        this.target = null;
    }

    public UUID getTarget() {
        return target;
    }

    public Player getTargetPlayer() {
        if (target == null)
            return null;
        return Bukkit.getPlayer(target);
    }

    public boolean isTarget(UUID playerId) {
        return target != null && target.equals(playerId);
    }

    public boolean isTarget(Player player) {
        return isTarget(player.getUniqueId());
    }

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

        if (plugin.getConfig().getBoolean("game.broadcast-events", true)) {
            Bukkit.broadcast(plugin.getMessage("game-started"));
        }

        return true;
    }

    public void stopGame() {
        gameActive = false;

        if (plugin.getConfig().getBoolean("game.auto-remove-on-end", true)) {
            plugin.getDisguiseManager().removeAllDisguises();
        }

        if (plugin.getConfig().getBoolean("game.broadcast-events", true)) {
            Bukkit.broadcast(plugin.getMessage("game-stopped"));
        }
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void clearAll() {
        stopGame();
        hunters.clear();
        target = null;
    }

    public void reload() {
    }
}
