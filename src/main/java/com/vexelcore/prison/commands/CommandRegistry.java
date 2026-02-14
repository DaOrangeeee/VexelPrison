package com.vexelcore.prison.commands;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class CommandRegistry implements CommandExecutor, TabCompleter, Listener {
    private final VexelPrisonPlugin plugin;

    public CommandRegistry(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        register("pickaxe");
        register("prestige");
        register("rebirth");
        register("crates");
        register("boosters");
        register("vexelcore");
        register("help");
        register("?");
    }

    private void register(String cmd) {
        PluginCommand command = plugin.getCommand(cmd);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("help") || label.equals("?")) {
            if (sender instanceof Player player) plugin.getGuiService().openHelp(player);
            return true;
        }
        if (!(sender instanceof Player player) && !label.equalsIgnoreCase("vexelcore")) return true;
        switch (label.toLowerCase()) {
            case "pickaxe" -> plugin.getGuiService().openUpgrades(player);
            case "prestige" -> openPrestige(player);
            case "rebirth" -> openRebirth(player);
            case "crates" -> player.sendMessage(ChatColor.GREEN + "Use crate GUI at spawn or /vexelcore givekey");
            case "boosters" -> player.sendMessage(ChatColor.YELLOW + "Booster GUI coming from /pickaxe menu integration.");
            case "vexelcore" -> return admin(sender, args);
            default -> { return false; }
        }
        return true;
    }

    public void openPrestige(Player player) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        if (plugin.getPrestigeService().prestige(data, 1)) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Prestiged to " + data.getPrestige());
        } else {
            player.sendMessage(ChatColor.RED + "You do not meet prestige requirements.");
        }
    }

    public void openRebirth(Player player) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        if (plugin.getRebirthService().rebirth(data)) {
            player.sendMessage(ChatColor.AQUA + "Rebirth successful. Permanent bonuses increased.");
        } else {
            player.sendMessage(ChatColor.RED + "You do not meet rebirth requirements.");
        }
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/vexelcore status|reload|givekey|setprestige|setrebirth|setpxlvl");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.reloadMessages();
                sender.sendMessage("Reloaded.");
            }
            case "status" -> {
                sender.sendMessage("§6VexelCore Status");
                for (Map.Entry<String, Boolean> e : plugin.getIntegrationManager().getStatus().entrySet()) {
                    sender.sendMessage((e.getValue() ? "§a✔ " : "§c✖ ") + e.getKey());
                }
            }
            case "givekey" -> {
                if (args.length < 4) return false;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) return true;
                plugin.getPlayerDataService().get(target).getVirtualKeys().merge(args[2].toLowerCase(), Integer.parseInt(args[3]), Integer::sum);
            }
            case "setprestige" -> setInt(sender, args, "prestige");
            case "setrebirth" -> setInt(sender, args, "rebirth");
            case "setpxlvl" -> setInt(sender, args, "pxlvl");
        }
        return true;
    }

    private void setInt(CommandSender sender, String[] args, String type) {
        if (args.length < 3) return;
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) return;
        int value = Integer.parseInt(args[2]);
        PlayerData data = plugin.getPlayerDataService().get(target);
        switch (type) {
            case "prestige" -> data.setPrestige(value);
            case "rebirth" -> data.setRebirth(value);
            case "pxlvl" -> data.setPickaxeLevel(value);
        }
        sender.sendMessage("Updated " + type + " for " + target.getName());
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!alias.equalsIgnoreCase("vexelcore") || args.length != 1) return java.util.Collections.emptyList();
        return java.util.List.of("givekey", "setprestige", "setrebirth", "setpxlvl", "reload", "status");
    }

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        if (cmd.equals("/help") || cmd.equals("/?")) {
            event.setCancelled(true);
            plugin.getGuiService().openHelp(event.getPlayer());
        }
    }
}
