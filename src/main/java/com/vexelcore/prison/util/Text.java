package com.vexelcore.prison.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class Text {
    private Text() {}

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public static List<String> color(List<String> lines) {
        List<String> out = new ArrayList<>();
        for (String line : lines) out.add(color(line));
        return out;
    }
}
