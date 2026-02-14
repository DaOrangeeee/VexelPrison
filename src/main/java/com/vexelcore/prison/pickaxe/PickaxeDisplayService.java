package com.vexelcore.prison.pickaxe;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PickaxeDisplayService {
    private final VexelPrisonPlugin plugin;
    private final Map<Player, BossBar> bars = new HashMap<>();

    public PickaxeDisplayService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player, PlayerData data) {
        int level = data.getPickaxeLevel();
        int req = (int) (Math.pow(level, 2) * plugin.getConfig().getDouble("pickaxe.level-formula-multiplier", 25.0));
        String msg = "§6Pickaxe §8» §eLvl " + level + " §7XP: §f" + data.getPickaxeXp() + "/" + req;
        String mode = plugin.getConfig().getString("pickaxe.display", "ACTIONBAR").toUpperCase();
        if (mode.equals("BOSSBAR")) {
            BossBar bar = bars.computeIfAbsent(player, p -> BossBar.bossBar(Component.text(msg), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));
            bar.name(Component.text(msg));
            bar.progress(Math.min(1f, (float) data.getPickaxeXp() / Math.max(1, req)));
            player.showBossBar(bar);
        } else {
            player.sendActionBar(Component.text(msg));
        }
    }
}
