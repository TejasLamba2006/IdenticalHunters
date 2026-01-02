package com.github.tejaslamba2006.identicalhunters.managers;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages player disguises - supports LibsDisguises or fallback stealth mode
 */
public class DisguiseManager {

    private final IdenticalHunters plugin;
    private DisguiseMode currentMode;
    private boolean libsDisguisesAvailable;
    private final Map<UUID, ItemStack[]> savedArmor;
    private final Set<UUID> disguisedPlayers;
    private BukkitTask invisibilityTask;

    public enum DisguiseMode {
        AUTO,
        DISGUISE,
        STEALTH
    }

    public DisguiseManager(IdenticalHunters plugin) {
        this.plugin = plugin;
        this.savedArmor = new HashMap<>();
        this.disguisedPlayers = new HashSet<>();

        // Check for LibsDisguises
        libsDisguisesAvailable = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;

        // Determine mode
        determineMode();

        // Start invisibility refresh task if using stealth mode
        if (currentMode == DisguiseMode.STEALTH) {
            startInvisibilityTask();
        }
    }

    /**
     * Determine which mode to use based on config and available plugins
     */
    private void determineMode() {
        String configMode = plugin.getConfig().getString("mode", "AUTO").toUpperCase();

        try {
            DisguiseMode requestedMode = DisguiseMode.valueOf(configMode);

            if (requestedMode == DisguiseMode.DISGUISE && !libsDisguisesAvailable) {
                plugin.getLogger()
                        .warning("DISGUISE mode requested but LibsDisguises not found! Falling back to STEALTH.");
                currentMode = DisguiseMode.STEALTH;
            } else if (requestedMode == DisguiseMode.AUTO) {
                currentMode = libsDisguisesAvailable ? DisguiseMode.DISGUISE : DisguiseMode.STEALTH;
            } else {
                currentMode = requestedMode;
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid mode in config: " + configMode + ". Using AUTO.");
            currentMode = libsDisguisesAvailable ? DisguiseMode.DISGUISE : DisguiseMode.STEALTH;
        }
    }

    /**
     * Get current disguise mode
     */
    public DisguiseMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Apply disguise to a player
     */
    public void applyDisguise(Player player) {
        if (player.hasPermission("identicalhunters.bypass")) {
            return;
        }

        if (disguisedPlayers.contains(player.getUniqueId())) {
            return; // Already disguised
        }

        if (currentMode == DisguiseMode.DISGUISE && libsDisguisesAvailable) {
            applyLibsDisguise(player);
        } else {
            applyStealthMode(player);
        }

        disguisedPlayers.add(player.getUniqueId());
    }

    /**
     * Remove disguise from a player
     */
    public void removeDisguise(Player player) {
        if (!disguisedPlayers.contains(player.getUniqueId())) {
            return; // Not disguised
        }

        if (currentMode == DisguiseMode.DISGUISE && libsDisguisesAvailable) {
            removeLibsDisguise(player);
        } else {
            removeStealthMode(player);
        }

        disguisedPlayers.remove(player.getUniqueId());
    }

    /**
     * Remove all active disguises
     */
    public void removeAllDisguises() {
        for (UUID playerId : new HashSet<>(disguisedPlayers)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeDisguise(player);
            }
        }
        disguisedPlayers.clear();
    }

    /**
     * Check if a player is disguised
     */
    public boolean isDisguised(Player player) {
        return disguisedPlayers.contains(player.getUniqueId());
    }

    // ==========================================
    // LIBS DISGUISES MODE
    // ==========================================

    /**
     * Apply LibsDisguises disguise
     */
    private void applyLibsDisguise(Player player) {
        try {
            String skinName = plugin.getConfig().getString("disguise-skin", "Dream");
            String displayName = plugin.getConfig().getString("disguise-name", "&cHunter");

            // Use reflection to avoid compile-time dependency
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            Class<?> playerDisguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.PlayerDisguise");

            // Create PlayerDisguise
            Object disguise = playerDisguiseClass.getConstructor(String.class).newInstance(skinName);

            // Set name if configured
            if (plugin.getConfig().getBoolean("disguise.show-name", true)) {
                playerDisguiseClass.getMethod("setName", String.class).invoke(disguise,
                        displayName.replace('&', '§'));
            }

            // Apply disguise
            disguiseApiClass.getMethod("disguiseToAll", org.bukkit.entity.Entity.class,
                    Class.forName("me.libraryaddict.disguise.disguisetypes.Disguise"))
                    .invoke(null, player, disguise);

            plugin.getLogger().info("Applied LibsDisguises disguise to " + player.getName());

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply LibsDisguises disguise: " + e.getMessage());
            // Fallback to stealth mode
            applyStealthMode(player);
        }
    }

    /**
     * Remove LibsDisguises disguise
     */
    private void removeLibsDisguise(Player player) {
        try {
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            disguiseApiClass.getMethod("undisguiseToAll", org.bukkit.entity.Entity.class)
                    .invoke(null, player);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove LibsDisguises disguise: " + e.getMessage());
        }
    }

    // ==========================================
    // STEALTH MODE (Fallback)
    // ==========================================

    /**
     * Apply stealth mode - invisibility + matching armor
     */
    private void applyStealthMode(Player player) {
        // Save current armor
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());

        // Apply invisibility
        if (plugin.getConfig().getBoolean("stealth.apply-invisibility", true)) {
            int duration = plugin.getConfig().getInt("stealth.invisibility-duration", 60) * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false, true));
        }

        // Apply matching armor
        if (plugin.getConfig().getBoolean("stealth.matching-armor.enabled", true)) {
            applyMatchingArmor(player);
        }

        // Apply player head
        if (plugin.getConfig().getBoolean("stealth.player-head.enabled", true)) {
            applyPlayerHead(player);
        }

        plugin.getLogger().info("Applied stealth mode to " + player.getName());
    }

    /**
     * Remove stealth mode
     */
    private void removeStealthMode(Player player) {
        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Restore saved armor
        ItemStack[] saved = savedArmor.remove(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setArmorContents(saved);
        }
    }

    /**
     * Apply matching armor to player
     */
    private void applyMatchingArmor(Player player) {
        String materialStr = plugin.getConfig().getString("stealth.matching-armor.material", "LEATHER").toUpperCase();

        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;

        String armorName = plugin.getConfig().getString("stealth.matching-armor.armor-name", "&cHunter Armor");
        Component armorNameComponent = plugin.colorize(armorName);

        // Get armor pieces based on material type
        switch (materialStr) {
            case "LEATHER" -> {
                helmet = createLeatherArmor(Material.LEATHER_HELMET, armorNameComponent);
                chestplate = createLeatherArmor(Material.LEATHER_CHESTPLATE, armorNameComponent);
                leggings = createLeatherArmor(Material.LEATHER_LEGGINGS, armorNameComponent);
                boots = createLeatherArmor(Material.LEATHER_BOOTS, armorNameComponent);
            }
            case "CHAINMAIL" -> {
                helmet = createArmor(Material.CHAINMAIL_HELMET, armorNameComponent);
                chestplate = createArmor(Material.CHAINMAIL_CHESTPLATE, armorNameComponent);
                leggings = createArmor(Material.CHAINMAIL_LEGGINGS, armorNameComponent);
                boots = createArmor(Material.CHAINMAIL_BOOTS, armorNameComponent);
            }
            case "IRON" -> {
                helmet = createArmor(Material.IRON_HELMET, armorNameComponent);
                chestplate = createArmor(Material.IRON_CHESTPLATE, armorNameComponent);
                leggings = createArmor(Material.IRON_LEGGINGS, armorNameComponent);
                boots = createArmor(Material.IRON_BOOTS, armorNameComponent);
            }
            case "DIAMOND" -> {
                helmet = createArmor(Material.DIAMOND_HELMET, armorNameComponent);
                chestplate = createArmor(Material.DIAMOND_CHESTPLATE, armorNameComponent);
                leggings = createArmor(Material.DIAMOND_LEGGINGS, armorNameComponent);
                boots = createArmor(Material.DIAMOND_BOOTS, armorNameComponent);
            }
            case "NETHERITE" -> {
                helmet = createArmor(Material.NETHERITE_HELMET, armorNameComponent);
                chestplate = createArmor(Material.NETHERITE_CHESTPLATE, armorNameComponent);
                leggings = createArmor(Material.NETHERITE_LEGGINGS, armorNameComponent);
                boots = createArmor(Material.NETHERITE_BOOTS, armorNameComponent);
            }
            case "GOLD", "GOLDEN" -> {
                helmet = createArmor(Material.GOLDEN_HELMET, armorNameComponent);
                chestplate = createArmor(Material.GOLDEN_CHESTPLATE, armorNameComponent);
                leggings = createArmor(Material.GOLDEN_LEGGINGS, armorNameComponent);
                boots = createArmor(Material.GOLDEN_BOOTS, armorNameComponent);
            }
            default -> {
                helmet = createLeatherArmor(Material.LEATHER_HELMET, armorNameComponent);
                chestplate = createLeatherArmor(Material.LEATHER_CHESTPLATE, armorNameComponent);
                leggings = createLeatherArmor(Material.LEATHER_LEGGINGS, armorNameComponent);
                boots = createLeatherArmor(Material.LEATHER_BOOTS, armorNameComponent);
            }
        }

        // Don't replace helmet if using player head
        if (!plugin.getConfig().getBoolean("stealth.player-head.enabled", true)) {
            player.getInventory().setHelmet(helmet);
        }

        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    /**
     * Create leather armor with color
     */
    private ItemStack createLeatherArmor(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.displayName(name);

        // Parse color from config
        String colorStr = plugin.getConfig().getString("stealth.matching-armor.leather-color", "255,0,0");
        Color color;

        try {
            if (colorStr.contains(",")) {
                String[] parts = colorStr.split(",");
                color = Color.fromRGB(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            } else {
                // Try to parse as color name
                color = switch (colorStr.toUpperCase()) {
                    case "RED" -> Color.RED;
                    case "BLUE" -> Color.BLUE;
                    case "GREEN" -> Color.GREEN;
                    case "YELLOW" -> Color.YELLOW;
                    case "PURPLE" -> Color.PURPLE;
                    case "ORANGE" -> Color.ORANGE;
                    case "WHITE" -> Color.WHITE;
                    case "BLACK" -> Color.BLACK;
                    default -> Color.RED;
                };
            }
        } catch (Exception e) {
            color = Color.RED;
        }

        meta.setColor(color);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Create regular armor piece
     */
    private ItemStack createArmor(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Apply player head to hunter
     */
    private void applyPlayerHead(Player player) {
        String skinName = plugin.getConfig().getString("disguise-skin", "Dream");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        // Set owning player
        OfflinePlayer skinPlayer = Bukkit.getOfflinePlayer(skinName);
        meta.setOwningPlayer(skinPlayer);

        String armorName = plugin.getConfig().getString("stealth.matching-armor.armor-name", "&cHunter Armor");
        meta.displayName(plugin.colorize(armorName));

        head.setItemMeta(meta);

        if (plugin.getConfig().getBoolean("stealth.player-head.force-equip", true)) {
            player.getInventory().setHelmet(head);
        }
    }

    /**
     * Start invisibility refresh task
     */
    private void startInvisibilityTask() {
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
        }

        int duration = plugin.getConfig().getInt("stealth.invisibility-duration", 60);
        int refreshInterval = (duration - 5) * 20; // Refresh 5 seconds before expiry

        if (refreshInterval < 20)
            refreshInterval = 20; // Minimum 1 second

        invisibilityTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfig().getBoolean("stealth.apply-invisibility", true))
                return;

            for (UUID playerId : disguisedPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline() && plugin.getHunterManager().isGameActive()) {
                    int dur = plugin.getConfig().getInt("stealth.invisibility-duration", 60) * 20;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur, 0, false, false, true));
                }
            }
        }, refreshInterval, refreshInterval);
    }

    /**
     * Reload disguise manager
     */
    public void reload() {
        // Cancel existing task
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
            invisibilityTask = null;
        }

        // Re-check available plugins
        libsDisguisesAvailable = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;

        // Re-determine mode
        determineMode();

        // Restart invisibility task if needed
        if (currentMode == DisguiseMode.STEALTH) {
            startInvisibilityTask();
        }
    }
}
