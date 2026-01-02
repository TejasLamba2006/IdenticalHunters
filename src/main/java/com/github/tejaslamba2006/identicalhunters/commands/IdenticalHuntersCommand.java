package com.github.tejaslamba2006.identicalhunters.commands;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import com.github.tejaslamba2006.identicalhunters.listeners.CompassListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main command handler for IdenticalHunters
 */
public class IdenticalHuntersCommand implements CommandExecutor, TabCompleter {
    
    private final IdenticalHunters plugin;
    
    public IdenticalHuntersCommand(IdenticalHunters plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            case "sethunter", "addhunter" -> handleSetHunter(sender, args);
            case "removehunter", "delhunter" -> handleRemoveHunter(sender, args);
            case "settarget" -> handleSetTarget(sender, args);
            case "removetarget" -> handleRemoveTarget(sender);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender);
            case "compass" -> handleCompass(sender, args);
            case "mode" -> handleMode(sender);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    /**
     * Start the hunt
     */
    private void handleStart(CommandSender sender) {
        if (plugin.getHunterManager().isGameActive()) {
            sender.sendMessage(plugin.colorize("&cThe hunt is already active!"));
            return;
        }
        
        if (plugin.getHunterManager().getHunterCount() == 0) {
            sender.sendMessage(plugin.getMessage("no-hunters"));
            return;
        }
        
        if (plugin.getHunterManager().startGame()) {
            // Give compasses if enabled
            if (plugin.getConfig().getBoolean("compass.give-compass", true)) {
                CompassListener compassListener = new CompassListener(plugin);
                for (UUID hunterId : plugin.getHunterManager().getHunters()) {
                    Player hunter = Bukkit.getPlayer(hunterId);
                    if (hunter != null && hunter.isOnline()) {
                        compassListener.giveTrackingCompass(hunter);
                    }
                }
            }
            sender.sendMessage(plugin.colorize("&aThe hunt has begun!"));
        } else {
            int min = plugin.getConfig().getInt("game.min-hunters", 1);
            sender.sendMessage(plugin.colorize("&cNeed at least " + min + " hunter(s) to start!"));
        }
    }
    
    /**
     * Stop the hunt
     */
    private void handleStop(CommandSender sender) {
        if (!plugin.getHunterManager().isGameActive()) {
            sender.sendMessage(plugin.colorize("&cNo hunt is currently active!"));
            return;
        }
        
        plugin.getHunterManager().stopGame();
        sender.sendMessage(plugin.colorize("&aThe hunt has been stopped."));
    }
    
    /**
     * Add a hunter
     */
    private void handleSetHunter(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /ih sethunter <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.colorize("&cPlayer '" + args[1] + "' not found or offline."));
            return;
        }
        
        if (plugin.getHunterManager().isTarget(target)) {
            sender.sendMessage(plugin.colorize("&cThat player is the target! Remove them as target first."));
            return;
        }
        
        if (plugin.getHunterManager().addHunter(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("hunter-added", "{player}", target.getName()));
            target.sendMessage(plugin.colorize("&aYou are now a hunter!"));
        } else {
            sender.sendMessage(plugin.getMessage("already-hunter", "{player}", target.getName()));
        }
    }
    
    /**
     * Remove a hunter
     */
    private void handleRemoveHunter(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /ih removehunter <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.colorize("&cPlayer '" + args[1] + "' not found or offline."));
            return;
        }
        
        if (plugin.getHunterManager().removeHunter(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("hunter-removed", "{player}", target.getName()));
            target.sendMessage(plugin.colorize("&cYou are no longer a hunter."));
        } else {
            sender.sendMessage(plugin.getMessage("not-hunter", "{player}", target.getName()));
        }
    }
    
    /**
     * Set the target (speedrunner)
     */
    private void handleSetTarget(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /ih settarget <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.colorize("&cPlayer '" + args[1] + "' not found or offline."));
            return;
        }
        
        if (plugin.getHunterManager().isHunter(target)) {
            sender.sendMessage(plugin.colorize("&cThat player is a hunter! Remove them as hunter first."));
            return;
        }
        
        plugin.getHunterManager().setTarget(target.getUniqueId());
        sender.sendMessage(plugin.getMessage("target-set", "{player}", target.getName()));
        target.sendMessage(plugin.colorize("&eYou are now the target! Run!"));
    }
    
    /**
     * Remove the target
     */
    private void handleRemoveTarget(CommandSender sender) {
        if (plugin.getHunterManager().getTarget() == null) {
            sender.sendMessage(plugin.colorize("&cNo target is currently set."));
            return;
        }
        
        plugin.getHunterManager().removeTarget();
        sender.sendMessage(plugin.getMessage("target-removed"));
    }
    
    /**
     * List hunters and target
     */
    private void handleList(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&8&m----------&r &c&lIdenticalHunters &8&m----------"));
        
        // Show target
        Player target = plugin.getHunterManager().getTargetPlayer();
        if (target != null) {
            sender.sendMessage(plugin.colorize("&eTarget: &f" + target.getName()));
        } else {
            sender.sendMessage(plugin.colorize("&eTarget: &7None set"));
        }
        
        // Show hunters
        sender.sendMessage(plugin.colorize("&cHunters: &7(" + plugin.getHunterManager().getHunterCount() + ")"));
        for (UUID hunterId : plugin.getHunterManager().getHunters()) {
            Player hunter = Bukkit.getPlayer(hunterId);
            String name = hunter != null ? hunter.getName() : hunterId.toString();
            String status = hunter != null && hunter.isOnline() ? "&a(Online)" : "&c(Offline)";
            String disguised = plugin.getDisguiseManager().isDisguised(hunter) ? " &d[Disguised]" : "";
            sender.sendMessage(plugin.colorize("  &7- &f" + name + " " + status + disguised));
        }
        
        // Show game status
        String gameStatus = plugin.getHunterManager().isGameActive() ? "&aActive" : "&cInactive";
        sender.sendMessage(plugin.colorize("&7Game Status: " + gameStatus));
        
        sender.sendMessage(plugin.colorize("&8&m----------------------------------------"));
    }
    
    /**
     * Show plugin info
     */
    private void handleInfo(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&8&m----------&r &c&lIdenticalHunters &8&m----------"));
        sender.sendMessage(plugin.colorize("&7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.colorize("&7Author: &f" + String.join(", ", plugin.getDescription().getAuthors())));
        sender.sendMessage(plugin.colorize("&7Mode: &f" + plugin.getDisguiseManager().getCurrentMode().name()));
        
        // Mode explanation
        switch (plugin.getDisguiseManager().getCurrentMode()) {
            case DISGUISE -> sender.sendMessage(plugin.colorize("&7Using &aLibsDisguises &7for full player disguises."));
            case STEALTH -> sender.sendMessage(plugin.colorize("&7Using &eStealth Mode &7(invisibility + matching armor)."));
            case AUTO -> sender.sendMessage(plugin.colorize("&7Auto-detecting best disguise method."));
        }
        
        // LibsDisguises status
        boolean hasLibs = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;
        String libsStatus = hasLibs ? "&aInstalled" : "&cNot Installed";
        sender.sendMessage(plugin.colorize("&7LibsDisguises: " + libsStatus));
        
        sender.sendMessage(plugin.colorize("&8&m----------------------------------------"));
    }
    
    /**
     * Give compass to player
     */
    private void handleCompass(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length < 2) {
                sender.sendMessage(plugin.colorize("&cUsage: /ih compass <player>"));
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.colorize("&cPlayer not found!"));
                return;
            }
            new CompassListener(plugin).giveTrackingCompass(target);
            sender.sendMessage(plugin.colorize("&aGave tracking compass to " + target.getName()));
            return;
        }
        
        Player target = player;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.colorize("&cPlayer not found!"));
                return;
            }
        }
        
        new CompassListener(plugin).giveTrackingCompass(target);
        if (target.equals(player)) {
            sender.sendMessage(plugin.colorize("&aYou received a tracking compass!"));
        } else {
            sender.sendMessage(plugin.colorize("&aGave tracking compass to " + target.getName()));
        }
    }
    
    /**
     * Show current mode
     */
    private void handleMode(CommandSender sender) {
        sender.sendMessage(plugin.getMessage("mode-" + plugin.getDisguiseManager().getCurrentMode().name().toLowerCase()));
    }
    
    /**
     * Reload configuration
     */
    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.getMessage("reload"));
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&8&m----------&r &c&lIdenticalHunters &8&m----------"));
        sender.sendMessage(plugin.colorize("&c/ih start &8- &7Start the hunt"));
        sender.sendMessage(plugin.colorize("&c/ih stop &8- &7Stop the hunt"));
        sender.sendMessage(plugin.colorize("&c/ih sethunter <player> &8- &7Add a hunter"));
        sender.sendMessage(plugin.colorize("&c/ih removehunter <player> &8- &7Remove a hunter"));
        sender.sendMessage(plugin.colorize("&c/ih settarget <player> &8- &7Set the target"));
        sender.sendMessage(plugin.colorize("&c/ih removetarget &8- &7Remove the target"));
        sender.sendMessage(plugin.colorize("&c/ih list &8- &7List hunters and target"));
        sender.sendMessage(plugin.colorize("&c/ih compass [player] &8- &7Give tracking compass"));
        sender.sendMessage(plugin.colorize("&c/ih info &8- &7Show plugin info"));
        sender.sendMessage(plugin.colorize("&c/ih mode &8- &7Show current disguise mode"));
        sender.sendMessage(plugin.colorize("&c/ih reload &8- &7Reload configuration"));
        sender.sendMessage(plugin.colorize("&8&m----------------------------------------"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions = Arrays.asList("start", "stop", "sethunter", "removehunter", "settarget", "removetarget", "list", "compass", "info", "mode", "reload");
            return filter(completions, args[0]);
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "sethunter", "addhunter", "settarget", "compass" -> {
                    return filter(getOnlinePlayerNames(), args[1]);
                }
                case "removehunter", "delhunter" -> {
                    return filter(getHunterNames(), args[1]);
                }
            }
        }
        
        return completions;
    }
    
    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }
    
    private List<String> getHunterNames() {
        return plugin.getHunterManager().getHunters().stream()
            .map(Bukkit::getPlayer)
            .filter(p -> p != null)
            .map(Player::getName)
            .collect(Collectors.toList());
    }
    
    private List<String> filter(List<String> list, String prefix) {
        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
