package com.vexelcore.prison.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {
    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lines = new ArrayList<>();
            for (String line : lore) lines.add(Text.color(line));
            meta.setLore(lines);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> editor) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            editor.accept(meta);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
