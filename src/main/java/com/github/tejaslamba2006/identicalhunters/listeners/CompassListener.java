package com.github.tejaslamba2006.identicalhunters.listeners;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles compass tracking functionality
 */
public class CompassListener implements Listener {
    
    private final IdenticalHunters plugin;
    private final Map<UUID, Long> lastCompassUse;
    private BukkitTask compassUpdateTask;
    
    public CompassListener(IdenticalHunters plugin) {
        this.plugin = plugin;
        this.lastCompassUse = new HashMap<>();
        
        // Start compass update task if enabled
        if (plugin.getConfig().getBoolean("compass.enabled", true)) {
            startCompassUpdateTask();
        }
    }
    
    /**
     * Handle compass right-click for manual update
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCompassUse(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("compass.enabled", true)) return;
        
        Player player = event.getPlayer();
        
        // Check if using right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if holding tracking compass
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }
        
        // Check if it's a tracking compass (check custom name)
        if (!isTrackingCompass(item)) {
            return;
        }
        
        // Check if player is a hunter
        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }
        
        // Rate limit - 1 second cooldown
        long now = System.currentTimeMillis();
        Long lastUse = lastCompassUse.get(player.getUniqueId());
        if (lastUse != null && now - lastUse < 1000) {
            return;
        }
        lastCompassUse.put(player.getUniqueId(), now);
        
        // Update compass
        updatePlayerCompass(player);
        
        // Feedback
        Player target = plugin.getHunterManager().getTargetPlayer();
        if (target != null && target.isOnline()) {
            double distance = player.getLocation().distance(target.getLocation());
            player.sendActionBar(Component.text("§eTracking " + target.getName() + " - §6" + String.format("%.1f", distance) + " blocks away"));
        } else {
            player.sendActionBar(Component.text("§cTarget not found or offline"));
        }
    }
    
    /**
     * Check if an item is a tracking compass
     */
    private boolean isTrackingCompass(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        var meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        
        // Check if the display name matches our compass name
        String compassName = plugin.getConfig().getString("compass.compass-name", "&6Tracking Compass");
        Component expectedName = plugin.colorize(compassName);
        
        return meta.displayName() != null && meta.displayName().equals(expectedName);
    }
    
    /**
     * Give a player a tracking compass
     */
    public void giveTrackingCompass(Player player) {
        ItemStack compass = createTrackingCompass();
        
        // Check if player already has a tracking compass
        for (ItemStack item : player.getInventory().getContents()) {
            if (isTrackingCompass(item)) {
                return; // Already has one
            }
        }
        
        player.getInventory().addItem(compass);
    }
    
    /**
     * Create a tracking compass item
     */
    public ItemStack createTrackingCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        
        // Set name
        String name = plugin.getConfig().getString("compass.compass-name", "&6Tracking Compass");
        meta.displayName(plugin.colorize(name));
        
        // Set lore
        List<String> loreStrings = plugin.getConfig().getStringList("compass.compass-lore");
        if (!loreStrings.isEmpty()) {
            List<Component> lore = loreStrings.stream()
                .map(plugin::colorize)
                .toList();
            meta.lore(lore);
        }
        
        compass.setItemMeta(meta);
        return compass;
    }
    
    /**
     * Update a player's compass to point to target
     */
    public void updatePlayerCompass(Player hunter) {
        if (!plugin.getConfig().getBoolean("compass.track-target", true)) return;
        
        Player target = plugin.getHunterManager().getTargetPlayer();
        if (target == null || !target.isOnline()) return;
        
        // Only track if in same world
        if (!hunter.getWorld().equals(target.getWorld())) {
            // Point to world spawn if in different world
            hunter.setCompassTarget(hunter.getWorld().getSpawnLocation());
            return;
        }
        
        Location targetLoc = target.getLocation();
        hunter.setCompassTarget(targetLoc);
        
        // Also update lodestone compass if held
        ItemStack mainHand = hunter.getInventory().getItemInMainHand();
        if (isTrackingCompass(mainHand)) {
            CompassMeta meta = (CompassMeta) mainHand.getItemMeta();
            meta.setLodestone(targetLoc);
            meta.setLodestoneTracked(false);
            mainHand.setItemMeta(meta);
        }
    }
    
    /**
     * Start automatic compass update task
     */
    private void startCompassUpdateTask() {
        int interval = plugin.getConfig().getInt("compass.update-interval", 20);
        
        compassUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getHunterManager().isGameActive()) return;
            
            Player target = plugin.getHunterManager().getTargetPlayer();
            if (target == null || !target.isOnline()) return;
            
            // Update compass for all hunters
            for (UUID hunterId : plugin.getHunterManager().getHunters()) {
                Player hunter = Bukkit.getPlayer(hunterId);
                if (hunter != null && hunter.isOnline()) {
                    updatePlayerCompass(hunter);
                }
            }
        }, interval, interval);
    }
    
    /**
     * Stop compass update task
     */
    public void stopCompassUpdateTask() {
        if (compassUpdateTask != null) {
            compassUpdateTask.cancel();
            compassUpdateTask = null;
        }
    }
}
