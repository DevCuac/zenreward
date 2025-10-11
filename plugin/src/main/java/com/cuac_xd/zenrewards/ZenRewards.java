package com.cuac_xd.zenrewards;

import com.cuac_xd.zenrewards.adapters.LegacyAdapter;
import com.cuac_xd.zenrewards.adapters.ModernAdapter;
import com.cuac_xd.zenrewards.api.VersionAdapter;
import com.cuac_xd.zenrewards.commands.RewardsCommand;
import com.cuac_xd.zenrewards.commands.StreakCommand;
import com.cuac_xd.zenrewards.commands.ZenRewardsAdminCommand;
import com.cuac_xd.zenrewards.configuration.ConfigManager;
import com.cuac_xd.zenrewards.listeners.InventoryClickListener;
import com.cuac_xd.zenrewards.listeners.InventoryCloseListener;
import com.cuac_xd.zenrewards.listeners.PlayerJoinListener;
import com.cuac_xd.zenrewards.managers.*;
import com.cuac_xd.zenrewards.placeholders.PAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ZenRewards extends JavaPlugin {

    private StreakMenuManager streakMenuManager;
    private static ZenRewards instance;
    private StreakManager streakManager;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private RewardManager rewardManager;
    private MenuManager menuManager;
    private VersionAdapter versionAdapter;
    private boolean papiEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        setupVersionAdapter();

        // 1. Cargar configuraciones
        this.configManager = new ConfigManager(this);
        configManager.loadFiles();

        // 2. Inicializar gestores
        this.streakMenuManager = new StreakMenuManager(this);
        this.streakManager = new StreakManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.rewardManager = new RewardManager(this);
        this.menuManager = new MenuManager(this);

        // 3. Carga el Hook con PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
            papiEnabled = true;
        }

        // 4. Registrar comandos
        getCommand("streak").setExecutor(new StreakCommand(this));
        getCommand("rewards").setExecutor(new RewardsCommand(this));
        getCommand("zenrewards").setExecutor(new ZenRewardsAdminCommand(this));

        // 5. Registrar listeners
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);

        getLogger().info("ZenRewards has been enabled successfully!");
    }

    private void setupVersionAdapter() {
        try {
            String versionString = Bukkit.getBukkitVersion().split("-")[0];
            int minorVersion = Integer.parseInt(versionString.split("\\.")[1]);

            if (minorVersion < 13) {
                this.versionAdapter = new LegacyAdapter();
            } else {
                this.versionAdapter = new ModernAdapter();
            }
        } catch (Exception e) {
            getLogger().severe("Could not determine server version! Defaulting to ModernAdapter.");
            e.printStackTrace();
            this.versionAdapter = new ModernAdapter();
        }
    }

    @Override
    public void onDisable() {
        if (menuManager != null) {
            menuManager.cancelAllTasks();
        }
        playerDataManager.saveAllPlayerData();
        getLogger().info("ZenRewards has been disabled.");
    }

    // Getters para acceder a los managers desde otras clases
    public static ZenRewards getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public StreakManager getStreakManager() { return streakManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public StreakMenuManager getStreakMenuManager() { return streakMenuManager; }
    public VersionAdapter getVersionAdapter() { return versionAdapter; }
    public boolean isPapiEnabled() {
        return papiEnabled;
    }
}