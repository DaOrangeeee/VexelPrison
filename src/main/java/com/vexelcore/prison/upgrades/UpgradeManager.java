package com.vexelcore.prison.upgrades;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

public class UpgradeManager {
    private final VexelPrisonPlugin plugin;

    public UpgradeManager(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public int getLevel(PlayerData data, UpgradeType type) {
        return data.getUpgrades().getOrDefault(type.name(), 0);
    }

    public void setLevel(PlayerData data, UpgradeType type, int value) {
        data.getUpgrades().put(type.name(), Math.max(0, Math.min(500, value)));
    }

    public long getCost(UpgradeType type, int nextLevel) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades." + type.name());
        double multiplier = sec == null ? 150.0 : sec.getDouble("cost-multiplier", 150.0);
        return (long) Math.max(1, Math.pow(nextLevel, 2) * multiplier);
    }
}
