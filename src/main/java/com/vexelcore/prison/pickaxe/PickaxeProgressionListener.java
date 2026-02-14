package com.vexelcore.prison.pickaxe;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import com.vexelcore.prison.upgrades.UpgradeType;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PickaxeProgressionListener implements Listener {
    private final VexelPrisonPlugin plugin;

    public PickaxeProgressionListener(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.getPickaxeManager().isPrisonPickaxe(event.getPlayer().getInventory().getItemInMainHand())) return;
        Block block = event.getBlock();
        if (!plugin.getMineRegionService().isInsideMine(block.getLocation())) return;
        if (block.hasMetadata("vp_placed")) return;

        PlayerData data = plugin.getPlayerDataService().get(event.getPlayer());
        int level = data.getPickaxeLevel();
        int gain = plugin.getConfig().getInt("pickaxe.base-xp", 5);
        int boost = plugin.getBoosterService().getMultiplierPercent(data, "XP") + plugin.getRebirthService().getXpBonusPercent(data);
        gain += (gain * boost / 100);
        data.setPickaxeXp(data.getPickaxeXp() + gain);
        data.getBlockStats().merge(block.getType().name(), 1, Integer::sum);

        int req;
        while (level < 500 && data.getPickaxeXp() >= (req = (int) (Math.pow(level, 2) * plugin.getConfig().getDouble("pickaxe.level-formula-multiplier", 25.0)))) {
            data.setPickaxeXp(data.getPickaxeXp() - req);
            level++;
            data.setPickaxeLevel(level);
        }

        plugin.getPickaxeDisplayService().update(event.getPlayer(), data);
        int keyFinder = data.getUpgrades().getOrDefault(UpgradeType.KEY_FINDER.name(), 0);
        if (keyFinder > 0 && Math.random() < keyFinder / 2000.0) {
            data.getVirtualKeys().merge("basic", 1, Integer::sum);
        }
    }
}
