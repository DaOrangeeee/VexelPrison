package com.vexelcore.prison.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

public final class ConfigUtil {
    private ConfigUtil() {}

    public static List<String> stringList(ConfigurationSection section, String path) {
        if (section == null) return Collections.emptyList();
        return section.getStringList(path);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
