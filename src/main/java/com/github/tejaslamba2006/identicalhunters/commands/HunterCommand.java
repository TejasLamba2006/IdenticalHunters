package com.github.tejaslamba2006.identicalhunters.commands;

import com.github.tejaslamba2006.identicalhunters.IdenticalHunters;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HunterCommand implements CommandExecutor, TabCompleter {

    private final IdenticalHunters plugin;

    public HunterCommand(IdenticalHunters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /hunter <add|remove> <player>"));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(plugin.colorize("&cPlayer '" + args[1] + "' not found or offline."));
            return true;
        }

        switch (action) {
            case "add" -> {
                if (plugin.getHunterManager().isTarget(target)) {
                    sender.sendMessage(plugin.colorize("&cThat player is the target!"));
                    return true;
                }

                if (plugin.getHunterManager().addHunter(target.getUniqueId())) {
                    sender.sendMessage(plugin.getMessage("hunter-added", "{player}", target.getName()));
                    target.sendMessage(plugin.colorize("&aYou are now a hunter!"));
                } else {
                    sender.sendMessage(plugin.getMessage("already-hunter", "{player}", target.getName()));
                }
            }
            case "remove" -> {
                if (plugin.getHunterManager().removeHunter(target.getUniqueId())) {
                    sender.sendMessage(plugin.getMessage("hunter-removed", "{player}", target.getName()));
                    target.sendMessage(plugin.colorize("&cYou are no longer a hunter."));
                } else {
                    sender.sendMessage(plugin.getMessage("not-hunter", "{player}", target.getName()));
                }
            }
            default -> sender.sendMessage(plugin.colorize("&cUsage: /hunter <add|remove> <player>"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("add", "remove"), args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                return filter(getHunterNames(), args[1]);
            }
            return filter(getOnlinePlayerNames(), args[1]);
        }

        return new ArrayList<>();
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
