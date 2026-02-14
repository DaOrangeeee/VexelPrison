package com.vexelcore.prison.prestige;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrestigeService {
    private final VexelPrisonPlugin plugin;

    public PrestigeService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public int maxPrestige() {
        return plugin.getConfig().getInt("prestige.max", 1000);
    }

    public boolean canPrestige(PlayerData data) {
        if (data.getPrestige() >= maxPrestige() || data.getPickaxeLevel() < 500) return false;
        int rebirthReq = plugin.getConfig().getInt("prestige.required-rebirth", 0);
        return data.getRebirth() >= rebirthReq;
    }

    public boolean prestige(PlayerData data, int amount) {
        if (amount <= 0) return false;
        int gained = 0;
        while (gained < amount && canPrestige(data)) {
            data.setPrestige(data.getPrestige() + 1);
            data.setPickaxeLevel(1);
            data.setPickaxeXp(0);
            gained++;
        }
        return gained > 0;
    }

    public List<String> rewardsForLevel(int level) {
        List<String> rewards = new ArrayList<>();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("prestige-rewards");
        if (root == null) return rewards;
        rewards.addAll(root.getStringList("default.every"));
        rewards.addAll(root.getStringList("milestones." + level));
        return rewards;
    }

    public boolean claimReward(Player player, PlayerData data, int level) {
        if (level <= 0 || level > data.getPrestige()) return false;
        String key = "prestige_claim_" + level;
        if (data.getClaims().getOrDefault(key, false)) return false;
        List<String> rewards = rewardsForLevel(level);
        if (rewards.isEmpty()) return false;
        for (String reward : rewards) applyReward(player, data, reward);
        data.getClaims().put(key, true);
        return true;
    }

    private void applyReward(Player player, PlayerData data, String reward) {
        String[] parts = reward.split(":");
        if (parts.length < 2) return;
        switch (parts[0].toLowerCase()) {
            case "money" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + parts[1]);
            case "tokens" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tokens add " + player.getName() + " " + parts[1]);
            case "key" -> {
                if (parts.length >= 3) data.getVirtualKeys().merge(parts[1].toLowerCase(), Integer.parseInt(parts[2]), Integer::sum);
            }
            case "booster" -> {
                if (parts.length >= 4) plugin.getBoosterService().activate(data, parts[1], Integer.parseInt(parts[2]), Long.parseLong(parts[3]));
            }
            case "command" -> {
                String command = reward.substring("command:".length()).replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}
