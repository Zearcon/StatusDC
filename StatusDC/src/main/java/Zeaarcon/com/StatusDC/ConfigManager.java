package Zeaarcon.com.StatusDC;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private static FileConfiguration config;
    private static StatusDC plugin;

    public static void load(StatusDC pl) {
        plugin = pl;
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public static void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public static String getMessage(String key) {
        return config.getString("messages." + key, "");
    }

    public static int getAfkTimeMillis() {
        return config.getInt("afk-time-minutes", 5) * 60 * 1000;
    }

    public static boolean useColors() {
        return config.getBoolean("name-format.use-colors", true);
    }

    public static String getWorldColor(World.Environment env) {
        String path = "name-format.world-colors." + env.name();
        String defaultColor = "&f";
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, defaultColor));
    }

    public static String getWorldBall(World.Environment env) {
        String path = "name-format.world-balls." + env.name();
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, "")) + ChatColor.RESET;
    }

    public static String getWorldPrefix(World.Environment env) {
        return (useColors() ? getWorldColor(env) : getWorldBall(env));
    }

    public static boolean isChatEnabled() {
        return config.getBoolean("chat.enabled", true);
    }

    public static String getChatFormat() {
        return config.getString("chat.format", "%status%%worldprefix%%player_name%&r: %message%");
    }

    public static String getStatusSymbol(String status) {
        return config.getString("statuses." + status + ".symbol", "");
    }

    public static String getStatusColor(String status) {
        String color = config.getString("statuses." + status + ".color", "&f");
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    public static List<String> getValidStatuses() {
        Set<String> keys = config.getConfigurationSection("statuses").getKeys(false);
        keys.remove("afk");
        return new ArrayList<>(keys);
    }
}