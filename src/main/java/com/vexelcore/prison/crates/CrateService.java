package com.vexelcore.prison.crates;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;

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

    public boolean openCrate(Player player, String crate) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        int keys = data.getVirtualKeys().getOrDefault(crate.toLowerCase(), 0);
        if (keys <= 0) return false;
        data.getVirtualKeys().put(crate.toLowerCase(), keys - 1);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " 1000");
        return true;
    }
}
