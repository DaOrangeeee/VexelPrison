package com.vexelcore.prison.prestige;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;

public class PrestigeService {
    private final VexelPrisonPlugin plugin;

    public PrestigeService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canPrestige(PlayerData data) {
        if (data.getPrestige() >= 1000 || data.getPickaxeLevel() < 500) return false;
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
}
