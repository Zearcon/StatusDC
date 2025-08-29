package Zeaarcon.com.StatusDC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class StatusManager implements Listener, CommandExecutor, TabCompleter {

    private static final Map<UUID, String> statuses = new HashMap<>();
    private static final Map<UUID, Long> lastMove = new HashMap<>();
    private static final Set<UUID> afkPlayers = new HashSet<>();

    public static String getStatus(Player player) {
        String baseStatus = statuses.getOrDefault(player.getUniqueId(), "");
        boolean isAfk = afkPlayers.contains(player.getUniqueId());
        StringBuilder sb = new StringBuilder();
        if (!baseStatus.isEmpty()) {
            sb.append(ConfigManager.getStatusColor(baseStatus))
                    .append(ConfigManager.getStatusSymbol(baseStatus))
                    .append(" ");
        }
        if (isAfk) {
            sb.append(ConfigManager.getStatusColor("afk"))
                    .append(ConfigManager.getStatusSymbol("afk"))
                    .append(" ");
        }
        return sb.toString().trim();
    }

    public static void setStatus(Player player, String statusKey) {
        UUID uuid = player.getUniqueId();
        if ("off".equalsIgnoreCase(statusKey)) {
            statuses.remove(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("status_disabled")));
        } else {
            statuses.put(uuid, statusKey.toLowerCase());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("status_set").replace("%status%", statusKey)));
        }
        updateDisplayName(player);
    }

    public static void setAFK(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        if (afk && afkPlayers.add(uuid)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("afk_enabled")));
        } else if (!afk && afkPlayers.remove(uuid)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("afk_disabled")));
        }
        updateDisplayName(player);
    }

    public static void checkAFK() {
        long now = System.currentTimeMillis();
        int afkMillis = ConfigManager.getAfkTimeMillis();
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            long last = lastMove.getOrDefault(uuid, now);
            setAFK(player, now - last > afkMillis);
        });
    }

    private static void updateDisplayName(Player player) {
        String status = getStatus(player);
        World.Environment env = player.getWorld().getEnvironment();
        String prefix = ConfigManager.useColors() ? ConfigManager.getWorldColor(env) : ConfigManager.getWorldBall(env);
        String displayName = (status.isEmpty() ? "" : status + " ") + prefix + player.getName();
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', displayName));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastMove.put(player.getUniqueId(), System.currentTimeMillis());
        updateDisplayName(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updateDisplayName(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        lastMove.put(uuid, System.currentTimeMillis());
        if (afkPlayers.contains(uuid)) {
            setAFK(player, false);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!ConfigManager.isChatEnabled()) return;
        Player player = event.getPlayer();
        String format = ConfigManager.getChatFormat();
        String worldPrefix = ConfigManager.getWorldPrefix(player.getWorld().getEnvironment());
        String formatted = format
                .replace("%status%", getStatus(player))
                .replace("%worldprefix%", worldPrefix)
                .replace("%player_name%", player.getName())
                .replace("%message%", event.getMessage());
        event.setFormat(ChatColor.translateAlternateColorCodes('&', formatted));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("statusspm.reload")) {
                ConfigManager.reload();
                Bukkit.getOnlinePlayers().forEach(StatusManager::updateDisplayName);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("config_reloaded")));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("no_permission")));
            }
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("player_only")));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("usage").replace("%statuses%", String.join("|", ConfigManager.getValidStatuses()))));
            return true;
        }
        String status = args[0].toLowerCase();
        List<String> validStatuses = ConfigManager.getValidStatuses();
        if (validStatuses.contains(status) || "off".equals(status)) {
            setStatus(player, status);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("invalid_status").replace("%statuses%", String.join(", ", validStatuses))));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>(ConfigManager.getValidStatuses());
            suggestions.add("off");
            if (sender.hasPermission("statusspm.reload")) {
                suggestions.add("reload");
            }
            return suggestions.stream().filter(s -> s.startsWith(input)).toList();
        }
        return Collections.emptyList();
    }
}