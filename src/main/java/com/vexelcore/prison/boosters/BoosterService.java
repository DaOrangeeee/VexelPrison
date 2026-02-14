package com.vexelcore.prison.boosters;

import com.vexelcore.prison.data.PlayerData;

public class BoosterService {
    public void activate(PlayerData data, String type, int percent, long durationSeconds) {
        data.getBoosters().put(type.toUpperCase() + "_PERCENT", (long) percent);
        data.getBoosters().put(type.toUpperCase() + "_UNTIL", System.currentTimeMillis() + (durationSeconds * 1000L));
    }

    public int getMultiplierPercent(PlayerData data, String type) {
        long until = data.getBoosters().getOrDefault(type.toUpperCase() + "_UNTIL", 0L);
        if (until < System.currentTimeMillis()) return 0;
        return data.getBoosters().getOrDefault(type.toUpperCase() + "_PERCENT", 0L).intValue();
    }
}
