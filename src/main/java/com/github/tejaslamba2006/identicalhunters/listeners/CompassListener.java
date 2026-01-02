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

public class CompassListener implements Listener {

    private final IdenticalHunters plugin;
    private final Map<UUID, Long> lastCompassUse;
    private BukkitTask compassUpdateTask;

    public CompassListener(IdenticalHunters plugin) {
        this.plugin = plugin;
        this.lastCompassUse = new HashMap<>();

        if (plugin.getConfig().getBoolean("compass.enabled", true)) {
            startCompassUpdateTask();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCompassUse(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("compass.enabled", true))
            return;

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }

        if (!isTrackingCompass(item)) {
            return;
        }

        if (!plugin.getHunterManager().isHunter(player)) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastUse = lastCompassUse.get(player.getUniqueId());
        if (lastUse != null && now - lastUse < 1000) {
            return;
        }
        lastCompassUse.put(player.getUniqueId(), now);

        updatePlayerCompass(player);

        Player target = plugin.getHunterManager().getTargetPlayer();
        if (target != null && target.isOnline()) {
            double distance = player.getLocation().distance(target.getLocation());
            player.sendActionBar(Component.text(
                    "§eTracking " + target.getName() + " - §6" + String.format("%.1f", distance) + " blocks away"));
        } else {
            player.sendActionBar(Component.text("§cTarget not found or offline"));
        }
    }

    private boolean isTrackingCompass(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;

        var meta = item.getItemMeta();
        if (!meta.hasDisplayName())
            return false;

        String compassName = plugin.getConfig().getString("compass.compass-name", "&6Tracking Compass");
        Component expectedName = plugin.colorize(compassName);

        return meta.displayName() != null && meta.displayName().equals(expectedName);
    }

    public void giveTrackingCompass(Player player) {
        ItemStack compass = createTrackingCompass();

        for (ItemStack item : player.getInventory().getContents()) {
            if (isTrackingCompass(item)) {
                return; // Already has one
            }
        }

        player.getInventory().addItem(compass);
    }

    public ItemStack createTrackingCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();

        String name = plugin.getConfig().getString("compass.compass-name", "&6Tracking Compass");
        meta.displayName(plugin.colorize(name));

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

    public void updatePlayerCompass(Player hunter) {
        if (!plugin.getConfig().getBoolean("compass.track-target", true))
            return;

        Player target = plugin.getHunterManager().getTargetPlayer();
        if (target == null || !target.isOnline())
            return;

        if (!hunter.getWorld().equals(target.getWorld())) {
            hunter.setCompassTarget(hunter.getWorld().getSpawnLocation());
            return;
        }

        Location targetLoc = target.getLocation();
        hunter.setCompassTarget(targetLoc);

        ItemStack mainHand = hunter.getInventory().getItemInMainHand();
        if (isTrackingCompass(mainHand)) {
            CompassMeta meta = (CompassMeta) mainHand.getItemMeta();
            meta.setLodestone(targetLoc);
            meta.setLodestoneTracked(false);
            mainHand.setItemMeta(meta);
        }
    }

    private void startCompassUpdateTask() {
        int interval = plugin.getConfig().getInt("compass.update-interval", 20);

        compassUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getHunterManager().isGameActive())
                return;

            Player target = plugin.getHunterManager().getTargetPlayer();
            if (target == null || !target.isOnline())
                return;

            for (UUID hunterId : plugin.getHunterManager().getHunters()) {
                Player hunter = Bukkit.getPlayer(hunterId);
                if (hunter != null && hunter.isOnline()) {
                    updatePlayerCompass(hunter);
                }
            }
        }, interval, interval);
    }

    public void stopCompassUpdateTask() {
        if (compassUpdateTask != null) {
            compassUpdateTask.cancel();
            compassUpdateTask = null;
        }
    }
}
