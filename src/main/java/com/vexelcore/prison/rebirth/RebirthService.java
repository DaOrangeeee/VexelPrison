package com.vexelcore.prison.rebirth;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;

public class RebirthService {
    private final VexelPrisonPlugin plugin;

    public RebirthService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canRebirth(PlayerData data) {
        return data.getPickaxeLevel() >= 500 && data.getRebirth() < plugin.getConfig().getInt("rebirth.max", 50);
    }

    public boolean rebirth(PlayerData data) {
        if (!canRebirth(data)) return false;
        data.setRebirth(data.getRebirth() + 1);
        data.setPickaxeLevel(1);
        data.setPickaxeXp(0);
        if (plugin.getConfig().getBoolean("rebirth.reset-upgrades", true)) {
            data.getUpgrades().replaceAll((k, v) -> 0);
        }
        return true;
    }

    public int getXpBonusPercent(PlayerData data) {
        return data.getRebirth() * plugin.getConfig().getInt("rebirth.bonuses.xp-per", 2);
    }
}
