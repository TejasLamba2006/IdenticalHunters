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

        libsDisguisesAvailable = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;

        determineMode();

        if (currentMode == DisguiseMode.STEALTH) {
            startInvisibilityTask();
        }
    }

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

    public DisguiseMode getCurrentMode() {
        return currentMode;
    }

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

    public void removeAllDisguises() {
        for (UUID playerId : new HashSet<>(disguisedPlayers)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeDisguise(player);
            }
        }
        disguisedPlayers.clear();
    }

    public boolean isDisguised(Player player) {
        return disguisedPlayers.contains(player.getUniqueId());
    }


    private void applyLibsDisguise(Player player) {
        try {
            String skinName = plugin.getConfig().getString("disguise-skin", "Dream");
            String displayName = plugin.getConfig().getString("disguise-name", "&cHunter");

            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            Class<?> playerDisguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.PlayerDisguise");

            Object disguise = playerDisguiseClass.getConstructor(String.class).newInstance(skinName);

            if (plugin.getConfig().getBoolean("disguise.show-name", true)) {
                playerDisguiseClass.getMethod("setName", String.class).invoke(disguise,
                        displayName.replace('&', '§'));
            }

            disguiseApiClass.getMethod("disguiseToAll", org.bukkit.entity.Entity.class,
                    Class.forName("me.libraryaddict.disguise.disguisetypes.Disguise"))
                    .invoke(null, player, disguise);

            plugin.getLogger().info("Applied LibsDisguises disguise to " + player.getName());

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply LibsDisguises disguise: " + e.getMessage());
            applyStealthMode(player);
        }
    }

    private void removeLibsDisguise(Player player) {
        try {
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            disguiseApiClass.getMethod("undisguiseToAll", org.bukkit.entity.Entity.class)
                    .invoke(null, player);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove LibsDisguises disguise: " + e.getMessage());
        }
    }


    private void applyStealthMode(Player player) {
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());

        if (plugin.getConfig().getBoolean("stealth.apply-invisibility", true)) {
            int duration = plugin.getConfig().getInt("stealth.invisibility-duration", 60) * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false, true));
        }

        if (plugin.getConfig().getBoolean("stealth.matching-armor.enabled", true)) {
            applyMatchingArmor(player);
        }

        if (plugin.getConfig().getBoolean("stealth.player-head.enabled", true)) {
            applyPlayerHead(player);
        }

        plugin.getLogger().info("Applied stealth mode to " + player.getName());
    }

    private void removeStealthMode(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        ItemStack[] saved = savedArmor.remove(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setArmorContents(saved);
        }
    }

    private void applyMatchingArmor(Player player) {
        String materialStr = plugin.getConfig().getString("stealth.matching-armor.material", "LEATHER").toUpperCase();

        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;

        String armorName = plugin.getConfig().getString("stealth.matching-armor.armor-name", "&cHunter Armor");
        Component armorNameComponent = plugin.colorize(armorName);

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

        if (!plugin.getConfig().getBoolean("stealth.player-head.enabled", true)) {
            player.getInventory().setHelmet(helmet);
        }

        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    private ItemStack createLeatherArmor(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.displayName(name);

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

    private ItemStack createArmor(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private void applyPlayerHead(Player player) {
        String skinName = plugin.getConfig().getString("disguise-skin", "Dream");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        OfflinePlayer skinPlayer = Bukkit.getOfflinePlayer(skinName);
        meta.setOwningPlayer(skinPlayer);

        String armorName = plugin.getConfig().getString("stealth.matching-armor.armor-name", "&cHunter Armor");
        meta.displayName(plugin.colorize(armorName));

        head.setItemMeta(meta);

        if (plugin.getConfig().getBoolean("stealth.player-head.force-equip", true)) {
            player.getInventory().setHelmet(head);
        }
    }

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

    public void reload() {
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
            invisibilityTask = null;
        }

        libsDisguisesAvailable = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;

        determineMode();

        if (currentMode == DisguiseMode.STEALTH) {
            startInvisibilityTask();
        }
    }
}
