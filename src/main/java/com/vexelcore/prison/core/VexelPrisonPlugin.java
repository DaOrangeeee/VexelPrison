package com.vexelcore.prison.core;

import com.vexelcore.prison.boosters.BoosterService;
import com.vexelcore.prison.commands.CommandRegistry;
import com.vexelcore.prison.crates.CrateService;
import com.vexelcore.prison.data.DatabaseManager;
import com.vexelcore.prison.data.PlayerDataService;
import com.vexelcore.prison.gui.GuiService;
import com.vexelcore.prison.integrations.IntegrationManager;
import com.vexelcore.prison.pickaxe.*;
import com.vexelcore.prison.prestige.PrestigeService;
import com.vexelcore.prison.rebirth.RebirthService;
import com.vexelcore.prison.upgrades.UpgradeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class VexelPrisonPlugin extends JavaPlugin implements Listener {
    private DatabaseManager databaseManager;
    private PlayerDataService playerDataService;
    private IntegrationManager integrationManager;
    private PickaxeManager pickaxeManager;
    private PickaxeDisplayService pickaxeDisplayService;
    private UpgradeManager upgradeManager;
    private RebirthService rebirthService;
    private PrestigeService prestigeService;
    private CrateService crateService;
    private BoosterService boosterService;
    private GuiService guiService;
    private MineRegionService mineRegionService;
    private CommandRegistry commands;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        reloadMessages();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        playerDataService = new PlayerDataService(this, databaseManager);
        integrationManager = new IntegrationManager(this);
        integrationManager.detect();
        integrationManager.runHealthCheck();

        pickaxeManager = new PickaxeManager(this);
        pickaxeDisplayService = new PickaxeDisplayService(this);
        upgradeManager = new UpgradeManager(this);
        rebirthService = new RebirthService(this);
        prestigeService = new PrestigeService(this);
        crateService = new CrateService(this);
        boosterService = new BoosterService();
        mineRegionService = new MineRegionService(this);
        guiService = new GuiService(this);
        commands = new CommandRegistry(this);
        commands.register();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PickaxeProtectionListener(pickaxeManager), this);
        Bukkit.getPluginManager().registerEvents(new PickaxeProgressionListener(this), this);
        Bukkit.getPluginManager().registerEvents(guiService, this);
        Bukkit.getPluginManager().registerEvents(commands, this);

        playerDataService.startAutoSave();
        Bukkit.getOnlinePlayers().forEach(this::loadAndBootstrap);
        getLogger().info("VexelPrisonCore enabled with modular core systems.");
    }

    @Override
    public void onDisable() {
        if (playerDataService != null) playerDataService.saveAll();
        if (databaseManager != null) databaseManager.close();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadAndBootstrap(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerDataService.unloadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.getBlock().setMetadata("vp_placed", new org.bukkit.metadata.FixedMetadataValue(this, true));
    }

    private void loadAndBootstrap(Player player) {
        playerDataService.loadPlayer(player);
        crateService.grantMonthly(playerDataService.get(player));
        pickaxeManager.ensureBound(player);
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public PlayerDataService getPlayerDataService() { return playerDataService; }
    public IntegrationManager getIntegrationManager() { return integrationManager; }
    public PickaxeManager getPickaxeManager() { return pickaxeManager; }
    public PickaxeDisplayService getPickaxeDisplayService() { return pickaxeDisplayService; }
    public UpgradeManager getUpgradeManager() { return upgradeManager; }
    public RebirthService getRebirthService() { return rebirthService; }
    public PrestigeService getPrestigeService() { return prestigeService; }
    public CrateService getCrateService() { return crateService; }
    public BoosterService getBoosterService() { return boosterService; }
    public GuiService getGuiService() { return guiService; }
    public MineRegionService getMineRegionService() { return mineRegionService; }
    public CommandRegistry getCommands() { return commands; }
    public FileConfiguration getMessages() { return messages; }
}
