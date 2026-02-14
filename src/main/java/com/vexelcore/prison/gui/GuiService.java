package com.vexelcore.prison.gui;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.vexelcore.prison.crates.CrateService;
import com.vexelcore.prison.data.PlayerData;
import com.vexelcore.prison.upgrades.UpgradeType;
import com.vexelcore.prison.util.ItemBuilder;
import com.vexelcore.prison.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiService implements Listener {
    private final VexelPrisonPlugin plugin;

    public GuiService(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(new TaggedHolder("main"), 27, Text.color(plugin.getConfig().getString("gui.main-title", "&8Vexel Prison")));
        fill(inv);
        inv.setItem(10, new ItemBuilder(Material.NETHERITE_PICKAXE).name("&6Pickaxe Upgrades").build());
        inv.setItem(12, new ItemBuilder(Material.NETHER_STAR).name("&dPrestige").build());
        inv.setItem(14, new ItemBuilder(Material.TOTEM_OF_UNDYING).name("&bRebirth").build());
        inv.setItem(16, new ItemBuilder(Material.CHEST).name("&6Crates").build());
        player.openInventory(inv);
    }

    public void openUpgrades(Player player) {
        Inventory inv = Bukkit.createInventory(new TaggedHolder("upgrades"), 54, Text.color(plugin.getConfig().getString("gui.upgrades-title", "&8Pickaxe Upgrades")));
        fill(inv);
        PlayerData data = plugin.getPlayerDataService().get(player);
        inv.setItem(4, new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&aPickaxe XP Wallet: &f" + data.getPickaxeXp()).build());
        int slot = 10;
        for (UpgradeType type : UpgradeType.values()) {
            int level = plugin.getUpgradeManager().getLevel(data, type);
            long cost = plugin.getUpgradeManager().getCost(type, level + 1);
            inv.setItem(slot++, new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&e" + type.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    .lore(List.of(
                            "&7Current: &f" + level + "/500",
                            "&7Next Cost: &b" + cost + " XP",
                            "&7Effect: &f" + describeEffect(type, level),
                            "&7Next: &a" + describeEffect(type, level + 1),
                            "&eClick = buy +1",
                            "&eShift-Click = buy max"
                    ))
                    .build());
            if (slot % 9 == 8) slot += 2;
        }
        inv.setItem(49, new ItemBuilder(Material.BARRIER).name("&cBack").build());
        player.openInventory(inv);
    }

    public void openPrestige(Player player, int page) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        int current = data.getPrestige();
        int start = Math.max(1, page * 45 - 44);
        int end = Math.min(plugin.getPrestigeService().maxPrestige(), start + 44);

        Inventory inv = Bukkit.createInventory(new TaggedHolder("prestige:" + page), 54, Text.color("&8Prestige Levels &7(Page " + page + ")"));
        fill(inv);
        int slot = 0;
        for (int level = start; level <= end; level++) {
            boolean owned = current >= level;
            boolean claimed = data.getClaims().getOrDefault("prestige_claim_" + level, false);
            Material mat = owned ? (claimed ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE) : Material.GRAY_DYE;
            inv.setItem(slot++, new ItemBuilder(mat)
                    .name((owned ? "&a" : "&7") + "Prestige " + level)
                    .lore(List.of(
                            "&7Status: " + (owned ? (claimed ? "&aClaimed" : "&eClaim available") : "&cLocked"),
                            "&7Rewards: &f" + String.join(", ", plugin.getPrestigeService().rewardsForLevel(level))
                    ))
                    .build());
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name("&e-10").build());
        inv.setItem(46, new ItemBuilder(Material.ARROW).name("&e-1 Page").build());
        inv.setItem(49, new ItemBuilder(Material.NETHER_STAR).name("&dPrestige Now (+1)").lore(List.of("&7Requires Pickaxe Level 500")).build());
        inv.setItem(52, new ItemBuilder(Material.ARROW).name("&e+1 Page").build());
        inv.setItem(53, new ItemBuilder(Material.ARROW).name("&e+10").build());
        player.openInventory(inv);
    }

    public void openRebirth(Player player) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        Inventory inv = Bukkit.createInventory(new TaggedHolder("rebirth"), 27, Text.color("&8Rebirth Confirmation"));
        fill(inv);
        inv.setItem(11, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("&cCancel").build());
        inv.setItem(13, new ItemBuilder(Material.BOOK)
                .name("&eRebirth Preview")
                .lore(List.of(
                        "&7Current Rebirth: &f" + data.getRebirth(),
                        "&7Requires Pickaxe Level: &f500",
                        "&7Will Reset: &cPickaxe level + upgrades",
                        "&7Gain Permanent XP Bonus: &a+" + plugin.getConfig().getInt("rebirth.bonuses.xp-per", 2) + "%"
                ))
                .build());
        inv.setItem(15, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&aConfirm Rebirth").build());
        player.openInventory(inv);
    }

    public void openCrates(Player player) {
        PlayerData data = plugin.getPlayerDataService().get(player);
        CrateService crates = plugin.getCrateService();
        Inventory inv = Bukkit.createInventory(new TaggedHolder("crates"), 27, Text.color("&8Virtual Crates"));
        fill(inv);
        inv.setItem(10, crateIcon("basic", crates.getKeys(data, "basic")));
        inv.setItem(12, crateIcon("rare", crates.getKeys(data, "rare")));
        inv.setItem(14, crateIcon("epic", crates.getKeys(data, "epic")));
        inv.setItem(16, crateIcon("monthly", crates.getKeys(data, "monthly")));
        player.openInventory(inv);
    }

    public void openHelp(Player player, String category) {
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("help-gui");
        Inventory inv = Bukkit.createInventory(new TaggedHolder("help:" + category), 54, Text.color(plugin.getConfig().getString("gui.help-title", "&8Server Help")));
        fill(inv);

        List<String> categories = root == null ? List.of("core") : new ArrayList<>(root.getKeys(false));
        int cSlot = 45;
        for (String cat : categories) {
            if (cSlot > 53) break;
            inv.setItem(cSlot++, new ItemBuilder(Material.BOOK)
                    .name((cat.equalsIgnoreCase(category) ? "&a" : "&7") + cat.toUpperCase(Locale.ROOT))
                    .build());
        }

        List<String> commands = root == null ? List.of("/pickaxe|Open upgrades") : root.getStringList(category + ".entries");
        int slot = 10;
        for (String entry : commands) {
            String[] parts = entry.split("\\|", 2);
            if (parts.length < 2) continue;
            inv.setItem(slot++, new ItemBuilder(Material.COMPASS).name("&a" + parts[0]).lore(List.of("&7" + parts[1], "&eClick to run")).build());
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }
        player.openInventory(inv);
    }

    private org.bukkit.inventory.ItemStack crateIcon(String crate, int keys) {
        return new ItemBuilder(Material.CHEST)
                .name("&6" + crate.substring(0, 1).toUpperCase() + crate.substring(1) + " Crate")
                .lore(List.of("&7Virtual Keys: &f" + keys, "&eClick to open")).build();
    }

    private String describeEffect(UpgradeType type, int level) {
        level = Math.max(0, Math.min(500, level));
        return switch (type) {
            case EFFICIENCY -> "Haste bonus " + level;
            case FORTUNE -> "Fortune proc " + (level * 0.15) + "%";
            case EXPLOSIVE -> "Blast radius " + Math.min(5, 2 + (level / 125));
            case LASER -> "Tunnel length " + Math.max(1, level / 25);
            case VEIN_MINER -> "Chain chance " + (level * 0.12) + "%";
            case TOKEN_FINDER -> "Token chance " + (level * 0.1) + "%";
            case KEY_FINDER -> "Key chance " + (level * 0.05) + "%";
            case AUTOSELL_BOOST -> "Sell boost " + (level * 0.2) + "%";
            case AUTOREPAIR -> "Repair chance " + (level * 0.1) + "%";
            case XP_MAGNET -> "XP gain " + (level * 0.2) + "%";
            case LUCKY_CHARM -> "Double proc " + (level * 0.08) + "%";
            case BLACKHOLE -> "Vacuum radius " + Math.max(1, level / 30);
            case GEM_FINDER -> "Gem chance " + (level * 0.06) + "%";
            case HASTE_SURGE -> "Burst chance " + (level * 0.05) + "%";
            case COMBO_STREAK -> "Combo multi " + (1 + (level * 0.003));
        };
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

        if (holder.tag.equals("main")) {
            if (event.getSlot() == 10) openUpgrades(player);
            if (event.getSlot() == 12) openPrestige(player, 1);
            if (event.getSlot() == 14) openRebirth(player);
            if (event.getSlot() == 16) openCrates(player);
            return;
        }

        if (holder.tag.equals("upgrades")) {
            if (event.getSlot() == 49) {
                openMain(player);
                return;
            }
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.ENCHANTED_BOOK) return;
            String display = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";
            String normalized = org.bukkit.ChatColor.stripColor(display).toUpperCase(Locale.ROOT).replace(' ', '_');
            try {
                UpgradeType type = UpgradeType.valueOf(normalized);
                PlayerData data = plugin.getPlayerDataService().get(player);
                int current = plugin.getUpgradeManager().getLevel(data, type);
                boolean reqLevelGate = plugin.getConfig().getBoolean("upgrades-require-pickaxe-level", true);
                int bought = 0;
                while (current < 500) {
                    if (!event.isShiftClick() && bought >= 1) break;
                    if (reqLevelGate && current + 1 > data.getPickaxeLevel()) break;
                    long cost = plugin.getUpgradeManager().getCost(type, current + 1);
                    if (data.getPickaxeXp() < cost) break;
                    data.setPickaxeXp((int) (data.getPickaxeXp() - cost));
                    current++;
                    bought++;
                }
                plugin.getUpgradeManager().setLevel(data, type, current);
                if (bought == 0) player.sendMessage("§cNot enough Pickaxe XP to upgrade.");
                else player.sendMessage("§aPurchased " + bought + " level(s) for " + type.name());
                openUpgrades(player);
            } catch (Exception ignored) {}
            return;
        }

        if (holder.tag.startsWith("prestige:")) {
            int page = Integer.parseInt(holder.tag.split(":")[1]);
            if (event.getSlot() == 49) {
                PlayerData data = plugin.getPlayerDataService().get(player);
                if (plugin.getPrestigeService().prestige(data, 1)) player.sendMessage("§dPrestiged to " + data.getPrestige());
                else player.sendMessage("§cYou do not meet prestige requirements.");
                openPrestige(player, page);
                return;
            }
            if (event.getSlot() == 45) { openPrestige(player, Math.max(1, page - 10)); return; }
            if (event.getSlot() == 46) { openPrestige(player, Math.max(1, page - 1)); return; }
            if (event.getSlot() == 52) { openPrestige(player, page + 1); return; }
            if (event.getSlot() == 53) { openPrestige(player, page + 10); return; }
            if (event.getSlot() < 45 && event.getCurrentItem() != null) {
                String name = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "");
                if (!name.startsWith("Prestige ")) return;
                int level = Integer.parseInt(name.replace("Prestige ", "").trim());
                PlayerData data = plugin.getPlayerDataService().get(player);
                if (plugin.getPrestigeService().claimReward(player, data, level)) player.sendMessage("§aClaimed rewards for Prestige " + level);
                else player.sendMessage("§cCannot claim this reward.");
                openPrestige(player, page);
            }
            return;
        }

        if (holder.tag.equals("rebirth")) {
            if (event.getSlot() == 15) {
                PlayerData data = plugin.getPlayerDataService().get(player);
                if (plugin.getRebirthService().rebirth(data)) player.sendMessage("§bRebirth successful. Permanent bonus increased.");
                else player.sendMessage("§cYou do not meet rebirth requirements.");
                openMain(player);
            }
            if (event.getSlot() == 11) openMain(player);
            return;
        }

        if (holder.tag.equals("crates")) {
            String crate = switch (event.getSlot()) {
                case 10 -> "basic";
                case 12 -> "rare";
                case 14 -> "epic";
                case 16 -> "monthly";
                default -> null;
            };
            if (crate != null) {
                if (!plugin.getCrateService().openCrate(player, crate)) player.sendMessage("§cYou don't have a key for this crate.");
                else player.closeInventory();
            }
            return;
        }

        if (holder.tag.startsWith("help:")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
            String display = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            ConfigurationSection root = plugin.getConfig().getConfigurationSection("help-gui");
            if (root != null && root.contains(display.toLowerCase(Locale.ROOT))) {
                openHelp(player, display.toLowerCase(Locale.ROOT));
                return;
            }
            if (display.startsWith("/")) player.performCommand(display.substring(1));
        }
    }

    private static class TaggedHolder implements InventoryHolder {
        private final String tag;
        private TaggedHolder(String tag) { this.tag = tag; }
        @Override public Inventory getInventory() { return null; }
    }
}
