package com.vexelcore.prison.pickaxe;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import com.vexelcore.prison.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PickaxeManager {
    private final VexelPrisonPlugin plugin;
    private final NamespacedKey pickaxeKey;

    public PickaxeManager(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
        this.pickaxeKey = new NamespacedKey(plugin, "prison_pickaxe");
    }

    public ItemStack createPickaxe(PlayerData data) {
        return new ItemBuilder(Material.NETHERITE_PICKAXE)
                .name("&6&lPrison Pickaxe &7[Lvl " + data.getPickaxeLevel() + "]")
                .lore(plugin.getMessages().getStringList("pickaxe.lore"))
                .flags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES)
                .meta(meta -> {
                    meta.setUnbreakable(true);
                    meta.getPersistentDataContainer().set(pickaxeKey, PersistentDataType.BYTE, (byte) 1);
                    if (meta instanceof Damageable dmg) dmg.setDamage(0);
                }).build();
    }

    public boolean isPrisonPickaxe(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(pickaxeKey, PersistentDataType.BYTE);
    }

    public void ensureBound(Player player) {
        ItemStack slot0 = player.getInventory().getItem(0);
        if (isPrisonPickaxe(slot0)) return;
        for (int i = 1; i < player.getInventory().getSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (isPrisonPickaxe(candidate)) {
                player.getInventory().setItem(i, slot0);
                player.getInventory().setItem(0, candidate);
                return;
            }
        }
        if (slot0 != null && !slot0.getType().isAir()) {
            player.getInventory().addItem(slot0);
        }
        player.getInventory().setItem(0, createPickaxe(plugin.getPlayerDataService().get(player)));
    }
}
