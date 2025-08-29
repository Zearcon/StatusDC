package Zeaarcon.com.StatusDC;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatusDCExpansion extends PlaceholderExpansion {

    private final StatusDC plugin;

    public StatusDCExpansion(StatusDC plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "statusdc";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Zearcon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";
        switch (identifier.toLowerCase()) {
            case "status":
                return StatusManager.getStatus(player);
            case "worldprefix":
                return ConfigManager.getWorldPrefix(player.getWorld().getEnvironment());
            case "worldcolor":
                return ConfigManager.getWorldColor(player.getWorld().getEnvironment());
            case "worldball":
                return ConfigManager.getWorldBall(player.getWorld().getEnvironment());
            case "ping":
                return formatPing(player.getPing());
            default:
                return null;
        }
    }

    private String formatPing(int ping) {
        ChatColor color = ping < 100 ? ChatColor.GREEN : (ping < 300 ? ChatColor.GOLD : ChatColor.RED);
        return color + String.valueOf(ping);
    }
}