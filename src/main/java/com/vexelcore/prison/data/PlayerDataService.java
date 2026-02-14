package com.vexelcore.prison.data;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataService {
    private final VexelPrisonPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataService(VexelPrisonPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public java.util.concurrent.CompletableFuture<PlayerData> loadPlayer(Player player) {
        return databaseManager.load(player.getUniqueId()).thenApply(data -> {
            cache.put(player.getUniqueId(), data);
            return data;
        });
    }

    public PlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), PlayerData::new);
    }

    public void savePlayer(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data != null) databaseManager.save(data);
    }

    public void saveAll() {
        cache.values().forEach(databaseManager::save);
    }

    public void unloadPlayer(Player player) {
        PlayerData data = cache.remove(player.getUniqueId());
        if (data != null) databaseManager.save(data);
    }

    public void startAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAll, 20L * 300, 20L * 300);
    }
}
