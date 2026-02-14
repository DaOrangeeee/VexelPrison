package com.vexelcore.prison.pickaxe;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

public class PickaxeProtectionListener implements Listener {
    private final PickaxeManager pickaxeManager;

    public PickaxeProtectionListener(PickaxeManager pickaxeManager) {
        this.pickaxeManager = pickaxeManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) { pickaxeManager.ensureBound(event.getPlayer()); }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) { pickaxeManager.ensureBound(event.getPlayer()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getItemDrop().getItemStack())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() != null && pickaxeManager.isPrisonPickaxe(event.getCurrentItem())) event.setCancelled(true);
        if (event.getSlot() == 0 && event.getClickedInventory() == player.getInventory()) event.setCancelled(true);
        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            if (event.getCurrentItem() != null && pickaxeManager.isPrisonPickaxe(event.getCurrentItem())) event.setCancelled(true);
            if (event.getHotbarButton() == 0) event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player p) {
                var hotbar = p.getInventory().getItem(event.getHotbarButton());
                if (pickaxeManager.isPrisonPickaxe(hotbar)) event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getOldCursor() != null && pickaxeManager.isPrisonPickaxe(event.getOldCursor())) event.setCancelled(true);
        if (event.getRawSlots().contains(0)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getMainHandItem()) || pickaxeManager.isPrisonPickaxe(event.getOffHandItem())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame && pickaxeManager.isPrisonPickaxe(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(PlayerItemDamageEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getItem())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvil(PrepareAnvilEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getInventory().getFirstItem()) || pickaxeManager.isPrisonPickaxe(event.getInventory().getSecondItem())) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrind(PrepareGrindstoneEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getInventory().getUpperItem()) || pickaxeManager.isPrisonPickaxe(event.getInventory().getLowerItem())) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (pickaxeManager.isPrisonPickaxe(event.getPlayer().getInventory().getItemInMainHand())) {
            pickaxeManager.ensureBound(event.getPlayer());
        }
    }
}
