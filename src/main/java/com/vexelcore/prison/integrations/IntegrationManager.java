package com.vexelcore.prison.integrations;

import com.vexelcore.prison.core.VexelPrisonPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class IntegrationManager {
    private final VexelPrisonPlugin plugin;
    private final Map<String, Boolean> status = new LinkedHashMap<>();

    public IntegrationManager(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void detect() {
        PluginManager pm = Bukkit.getPluginManager();
        status.put("Vault", pm.isPluginEnabled("Vault"));
        status.put("LuckPerms", pm.isPluginEnabled("LuckPerms"));
        status.put("PlaceholderAPI", pm.isPluginEnabled("PlaceholderAPI"));
        status.put("WorldGuard", pm.isPluginEnabled("WorldGuard"));
        status.put("ItemsAdder", pm.isPluginEnabled("ItemsAdder"));
        status.put("DecentHolograms", pm.isPluginEnabled("DecentHolograms") || pm.isPluginEnabled("HolographicDisplays"));
    }

    public Map<String, Boolean> getStatus() {
        return status;
    }

    public boolean isEnabled(String integration) {
        return status.getOrDefault(integration, false);
    }

    public boolean runHealthCheck() {
        boolean db = plugin.getDatabaseManager().isConnected();
        status.put("Database", db);
        return db;
    }
}
