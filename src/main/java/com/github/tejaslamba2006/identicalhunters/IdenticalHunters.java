package com.github.tejaslamba2006.identicalhunters;

import com.github.tejaslamba2006.identicalhunters.commands.HunterCommand;
import com.github.tejaslamba2006.identicalhunters.commands.IdenticalHuntersCommand;
import com.github.tejaslamba2006.identicalhunters.listeners.CompassListener;
import com.github.tejaslamba2006.identicalhunters.listeners.HunterListener;
import com.github.tejaslamba2006.identicalhunters.managers.DisguiseManager;
import com.github.tejaslamba2006.identicalhunters.managers.HunterManager;
import com.github.tejaslamba2006.identicalhunters.utils.PluginAdvertiser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public class IdenticalHunters extends JavaPlugin {

    private static IdenticalHunters instance;
    private HunterManager hunterManager;
    private DisguiseManager disguiseManager;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();

        // Initialize managers
        hunterManager = new HunterManager(this);
        disguiseManager = new DisguiseManager(this);

        // Register commands
        IdenticalHuntersCommand mainCommand = new IdenticalHuntersCommand(this);
        getCommand("identicalhunters").setExecutor(mainCommand);
        getCommand("identicalhunters").setTabCompleter(mainCommand);

        HunterCommand hunterCommand = new HunterCommand(this);
        getCommand("hunter").setExecutor(hunterCommand);
        getCommand("hunter").setTabCompleter(hunterCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new HunterListener(this), this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);

        // Show advertisement
        if (getConfig().getBoolean("show-advertisement", true)) {
            PluginAdvertiser.showEnableMessage(this);
        }

        // Log mode info
        String mode = disguiseManager.getCurrentMode().name();
        getLogger().info("IdenticalHunters enabled! Using " + mode + " mode.");

        if (disguiseManager.getCurrentMode() == DisguiseManager.DisguiseMode.STEALTH) {
            getLogger().info("Stealth mode: Hunters will have invisibility + matching armor.");
        } else if (disguiseManager.getCurrentMode() == DisguiseManager.DisguiseMode.DISGUISE) {
            getLogger().info("Disguise mode: Using LibsDisguises for full player disguises.");
        }
    }

    @Override
    public void onDisable() {
        // Clean up all disguises
        if (disguiseManager != null) {
            disguiseManager.removeAllDisguises();
        }

        // Clear hunter data
        if (hunterManager != null) {
            hunterManager.clearAll();
        }

        getLogger().info("IdenticalHunters disabled! All disguises removed.");
    }

    /**
     * Get plugin instance
     */
    public static IdenticalHunters getInstance() {
        return instance;
    }

    /**
     * Get hunter manager
     */
    public HunterManager getHunterManager() {
        return hunterManager;
    }

    /**
     * Get disguise manager
     */
    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    /**
     * Reload configuration
     */
    public void reload() {
        reloadConfig();
        disguiseManager.reload();
        hunterManager.reload();
    }

    /**
     * Colorize text with legacy color codes and MiniMessage
     */
    public Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // First convert legacy color codes to section symbols
        String legacyText = text.replace('&', '§');

        // Check if text contains MiniMessage tags
        if (text.contains("<") && text.contains(">")) {
            // Parse as MiniMessage
            return miniMessage.deserialize(text);
        }

        // Parse legacy format
        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }

    /**
     * Get a message from config with prefix
     */
    public Component getMessage(String key, String... replacements) {
        String prefix = getConfig().getString("messages.prefix", "&8[&cIdenticalHunters&8] &7");
        String message = getConfig().getString("messages." + key, key);

        // Apply replacements (pairs of find/replace)
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return colorize(prefix + message);
    }

    /**
     * Get a raw message without prefix
     */
    public Component getRawMessage(String key, String... replacements) {
        String message = getConfig().getString("messages." + key, key);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return colorize(message);
    }
}
