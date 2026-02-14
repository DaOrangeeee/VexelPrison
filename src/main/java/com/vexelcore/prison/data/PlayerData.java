package com.vexelcore.prison.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int pickaxeXp;
    private int pickaxeLevel;
    private int rebirth;
    private int prestige;
    private long playtimeSeconds;
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Map<String, Integer> virtualKeys = new HashMap<>();
    private final Map<String, Long> boosters = new HashMap<>();
    private final Map<String, Integer> blockStats = new HashMap<>();
    private final Map<String, Boolean> claims = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.pickaxeLevel = 1;
    }

    public UUID getUuid() { return uuid; }
    public int getPickaxeXp() { return pickaxeXp; }
    public void setPickaxeXp(int pickaxeXp) { this.pickaxeXp = Math.max(0, pickaxeXp); }
    public int getPickaxeLevel() { return pickaxeLevel; }
    public void setPickaxeLevel(int pickaxeLevel) { this.pickaxeLevel = Math.max(1, pickaxeLevel); }
    public int getRebirth() { return rebirth; }
    public void setRebirth(int rebirth) { this.rebirth = Math.max(0, rebirth); }
    public int getPrestige() { return prestige; }
    public void setPrestige(int prestige) { this.prestige = Math.max(0, prestige); }
    public long getPlaytimeSeconds() { return playtimeSeconds; }
    public void setPlaytimeSeconds(long playtimeSeconds) { this.playtimeSeconds = Math.max(0, playtimeSeconds); }
    public Map<String, Integer> getUpgrades() { return upgrades; }
    public Map<String, Integer> getVirtualKeys() { return virtualKeys; }
    public Map<String, Long> getBoosters() { return boosters; }
    public Map<String, Integer> getBlockStats() { return blockStats; }
    public Map<String, Boolean> getClaims() { return claims; }
}
