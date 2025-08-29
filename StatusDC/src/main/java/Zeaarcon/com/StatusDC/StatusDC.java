package Zeaarcon.com.StatusDC;

import org.bukkit.plugin.java.JavaPlugin;

public class StatusDC extends JavaPlugin {

    private static StatusDC instance;
    private StatusManager statusManager;

    public static StatusDC getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.load(this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatusDCExpansion(this).register();
            getLogger().info(ConfigManager.getMessage("placeholderapi_detected"));
        } else {
            getLogger().warning(ConfigManager.getMessage("placeholderapi_not_found"));
        }
        statusManager = new StatusManager();
        getServer().getPluginManager().registerEvents(statusManager, this);
        getCommand("status").setExecutor(statusManager);
        getCommand("status").setTabCompleter(statusManager);
        getServer().getScheduler().runTaskTimer(this, StatusManager::checkAFK, 20L, 20L);
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}