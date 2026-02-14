package com.vexelcore.prison.gui;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.data.PlayerData;
import com.vexelcore.prison.upgrades.UpgradeType;
import com.vexelcore.prison.util.ItemBuilder;
import com.vexelcore.prison.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Locale;

public class GuiService implements Listener {
    private final VexelPrisonPlugin plugin;

    public GuiService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(new TaggedHolder("main"), 27, Text.color(plugin.getConfig().getString("gui.main-title", "&8Vexel Prison")));
        fill(inv);
        inv.setItem(11, new ItemBuilder(Material.NETHERITE_PICKAXE).name("&6Pickaxe Upgrades").build());
        inv.setItem(13, new ItemBuilder(Material.NETHER_STAR).name("&dPrestige").build());
        inv.setItem(15, new ItemBuilder(Material.TOTEM_OF_UNDYING).name("&bRebirth").build());
        player.openInventory(inv);
    }

    public void openUpgrades(Player player) {
        Inventory inv = Bukkit.createInventory(new TaggedHolder("upgrades"), 54, Text.color(plugin.getConfig().getString("gui.upgrades-title", "&8Pickaxe Upgrades")));
        fill(inv);
        PlayerData data = plugin.getPlayerDataService().get(player);
        int slot = 10;
        for (UpgradeType type : UpgradeType.values()) {
            int level = plugin.getUpgradeManager().getLevel(data, type);
            long cost = plugin.getUpgradeManager().getCost(type, level + 1);
            inv.setItem(slot++, new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&e" + type.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    .lore(java.util.List.of("&7Current: &f" + level, "&7Next Cost: &6$" + cost, "&eClick to buy +1", "&eShift-Click to buy max"))
                    .build());
            if (slot % 9 == 8) slot += 2;
        }
        player.openInventory(inv);
    }

    public void openHelp(Player player) {
        Inventory inv = Bukkit.createInventory(new TaggedHolder("help"), 45, Text.color(plugin.getConfig().getString("gui.help-title", "&8Server Help")));
        fill(inv);
        inv.setItem(20, new ItemBuilder(Material.COMPASS).name("&a/pickaxe").lore(java.util.List.of("&7Open pickaxe upgrades")).build());
        inv.setItem(22, new ItemBuilder(Material.NETHER_STAR).name("&d/prestige").lore(java.util.List.of("&7Open prestige menu")).build());
        inv.setItem(24, new ItemBuilder(Material.CHEST).name("&6/crates").lore(java.util.List.of("&7Open crate menu")).build());
        player.openInventory(inv);
    }

    private void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TaggedHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        switch (holder.tag) {
            case "main" -> {
                if (event.getSlot() == 11) openUpgrades(player);
                if (event.getSlot() == 13) plugin.getCommands().openPrestige(player);
                if (event.getSlot() == 15) plugin.getCommands().openRebirth(player);
            }
            case "upgrades" -> {
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.ENCHANTED_BOOK) return;
                String display = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";
                String normalized = org.bukkit.ChatColor.stripColor(display).toUpperCase(Locale.ROOT).replace(' ', '_');
                try {
                    UpgradeType type = UpgradeType.valueOf(normalized);
                    PlayerData data = plugin.getPlayerDataService().get(player);
                    int current = plugin.getUpgradeManager().getLevel(data, type);
                    int buy = event.isShiftClick() ? Math.max(1, data.getPickaxeLevel() - current) : 1;
                    plugin.getUpgradeManager().setLevel(data, type, current + buy);
                    openUpgrades(player);
                } catch (Exception ignored) {}
            }
            case "help" -> {
                if (event.getSlot() == 20) player.performCommand("pickaxe");
                if (event.getSlot() == 22) player.performCommand("prestige");
                if (event.getSlot() == 24) player.performCommand("crates");
            }
        }
    }

    private static class TaggedHolder implements InventoryHolder {
        private final String tag;
        private TaggedHolder(String tag) { this.tag = tag; }
        @Override public Inventory getInventory() { return null; }
    }
}
