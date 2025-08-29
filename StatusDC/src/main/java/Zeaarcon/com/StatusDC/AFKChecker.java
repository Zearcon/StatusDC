package Zeaarcon.com.StatusDC;

import org.bukkit.scheduler.BukkitRunnable;

public class AFKChecker {

    public AFKChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                StatusManager.checkAFK();
            }
        }.runTaskTimer(StatusDC.getInstance(), 20 * 60, 20 * 60); // Каждую минуту
    }
}
