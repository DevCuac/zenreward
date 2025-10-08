package com.cuac_xd.zenrewards.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final UUID playerUUID;
    private final Map<String, Long> rewardCooldowns;
    private boolean notificationsEnabled;
    private boolean autoClaimEnabled;
    private int connectionStreak;
    private long lastLogin;

    // LÍNEA QUE FALTABA: Declaración de la variable
    private int lastStreakClaimedDay;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.rewardCooldowns = new ConcurrentHashMap<>(); // Es buena práctica mantenerlo concurrente
        this.notificationsEnabled = true;
        this.autoClaimEnabled = false;

        // Inicialización de los nuevos campos
        this.connectionStreak = 0;
        this.lastLogin = 0;
        this.lastStreakClaimedDay = 0; // Esta línea ahora funcionará
    }

    // Getters y Setters
    public UUID getPlayerUUID() { return playerUUID; }

    public long getCooldown(String rewardId) {
        return rewardCooldowns.getOrDefault(rewardId, 0L);
    }

    public void setCooldown(String rewardId, long timestamp) {
        rewardCooldowns.put(rewardId, timestamp);
    }

    public void clearCooldown(String rewardId) {
        rewardCooldowns.remove(rewardId);
    }

    public Map<String, Long> getRewardCooldowns() { return rewardCooldowns; }

    public boolean hasNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public boolean hasAutoClaimEnabled() { return autoClaimEnabled; }
    public void setAutoClaimEnabled(boolean autoClaimEnabled) { this.autoClaimEnabled = autoClaimEnabled; }

    // Getters y Setters para la Racha
    public int getConnectionStreak() { return connectionStreak; }
    public void setConnectionStreak(int connectionStreak) { this.connectionStreak = connectionStreak; }

    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    // Getters y Setters para el día de racha reclamado
    public int getLastStreakClaimedDay() { return lastStreakClaimedDay; }
    public void setLastStreakClaimedDay(int lastStreakClaimedDay) { this.lastStreakClaimedDay = lastStreakClaimedDay; }
}