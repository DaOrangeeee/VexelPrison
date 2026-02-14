package com.vexelcore.prison.core;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class MineRegionService {
    private final Map<String, Cuboid> cuboids = new HashMap<>();

    public MineRegionService(VexelPrisonPlugin plugin) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("mines");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            String world = sec.getString(key + ".world", "world");
            int x1 = sec.getInt(key + ".x1");
            int y1 = sec.getInt(key + ".y1");
            int z1 = sec.getInt(key + ".z1");
            int x2 = sec.getInt(key + ".x2");
            int y2 = sec.getInt(key + ".y2");
            int z2 = sec.getInt(key + ".z2");
            cuboids.put(key, new Cuboid(world, x1, y1, z1, x2, y2, z2));
        }
    }

    public boolean isInsideMine(Location location) {
        return cuboids.values().stream().anyMatch(c -> c.contains(location));
    }

    private record Cuboid(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        boolean contains(Location loc) {
            if (loc.getWorld() == null || !loc.getWorld().getName().equalsIgnoreCase(world)) return false;
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
            return loc.getBlockX() >= minX && loc.getBlockX() <= maxX && loc.getBlockY() >= minY && loc.getBlockY() <= maxY && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
        }
    }
}
