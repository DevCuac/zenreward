package com.cuac_xd.zenrewards.models;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;

public class StreakReward {
    private final int day;
    private final List<String> commands;
    private final ConfigurationSection displayConfig; // Renombrado de 'displayItem' a 'displayConfig'

    public StreakReward(int day, List<String> commands, ConfigurationSection displayConfig) {
        this.day = day;
        this.commands = commands;
        this.displayConfig = displayConfig;
    }

    public int getDay() { return day; }
    public List<String> getCommands() { return commands; }

    // Getter corregido
    public ConfigurationSection getDisplayConfig() { return displayConfig; }
}