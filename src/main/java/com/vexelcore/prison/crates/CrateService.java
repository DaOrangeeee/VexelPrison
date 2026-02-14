package com.vexelcore.prison.crates;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CrateService {
    private final VexelPrisonPlugin plugin;

    public CrateService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantMonthly(PlayerData data) {
        String flag = "monthly_" + LocalDate.now().getYear() + "_" + LocalDate.now().getMonthValue();
        if (!data.getClaims().getOrDefault(flag, false)) {
            data.getVirtualKeys().merge("monthly", 1, Integer::sum);
            data.getClaims().put(flag, true);
        }
    }

    public int getKeys(PlayerData data, String crate) {
        return data.getVirtualKeys().getOrDefault(crate.toLowerCase(), 0);
    }

    public boolean openCrate(Player player, String crate) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        crate = crate.toLowerCase();
        int keys = data.getVirtualKeys().getOrDefault(crate, 0);
        if (keys <= 0) return false;

        Reward reward = roll(crate);
        if (reward == null) return false;
        data.getVirtualKeys().put(crate, keys - 1);

        long delayTicks = plugin.getConfig().getLong("crates." + crate + ".spin-seconds", 4) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> applyReward(player, data, reward.raw), delayTicks);
        player.sendMessage("§6Opening " + crate + " crate...");
        return true;
    }

    private Reward roll(String crate) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("crates." + crate);
        if (sec == null) return null;
        List<String> entries = sec.getStringList("rewards");
        List<Reward> rewards = new ArrayList<>();
        int totalWeight = 0;
        for (String entry : entries) {
            int weight = parseWeight(entry);
            totalWeight += weight;
            rewards.add(new Reward(entry, weight));
        }
        if (totalWeight <= 0 || rewards.isEmpty()) return null;
        int roll = ThreadLocalRandom.current().nextInt(totalWeight) + 1;
        int running = 0;
        for (Reward reward : rewards) {
            running += reward.weight;
            if (roll <= running) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    private int parseWeight(String entry) {
        int idx = entry.lastIndexOf("weight=");
        if (idx == -1) return 1;
        try { return Math.max(1, Integer.parseInt(entry.substring(idx + 7))); } catch (Exception e) { return 1; }
    }

    private void applyReward(Player player, PlayerData data, String raw) {
        String payload = raw.replaceAll(":weight=\\d+$", "");
        String[] parts = payload.split(":");
        if (parts.length < 2) return;

        switch (parts[0].toLowerCase()) {
            case "money" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + parts[1]);
            case "tokens" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tokens add " + player.getName() + " " + parts[1]);
            case "booster" -> {
                if (parts.length >= 4) plugin.getBoosterService().activate(data, parts[1], Integer.parseInt(parts[2]), Long.parseLong(parts[3]));
            }
            case "key" -> {
                if (parts.length >= 3) data.getVirtualKeys().merge(parts[1].toLowerCase(), Integer.parseInt(parts[2]), Integer::sum);
            }
            case "command" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), payload.substring("command:".length()).replace("%player%", player.getName()));
            case "rankchance" -> {
                if (parts.length >= 3) {
                    double chance = Double.parseDouble(parts[2]);
                    if (ThreadLocalRandom.current().nextDouble(100) <= chance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add " + parts[1]);
                    }
                }
            }
        }

        player.sendMessage("§aCrate Reward: §f" + payload);
        if (payload.toLowerCase().contains("rankchance") || payload.toLowerCase().contains("command:lp user")) {
            Bukkit.broadcastMessage("§d[Crates] " + player.getName() + " won a rare reward!");
        }
    }

    private record Reward(String raw, int weight) {}
}
