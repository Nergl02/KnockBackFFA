package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {

	private final NerKubKnockBackFFA plugin;
	private BossBar bossBar;
	private int timeRemaining;
	private String currentArena;

	public BossBarManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		this.timeRemaining = plugin.getTimeRemaining();
		this.currentArena = plugin.getArenaManager().getCurrentArenaName();

		if (plugin.getConfig().getBoolean("boss-bar", true)) {
			createBossBar();
		}
	}

	public void createBossBar() {
		if (bossBar != null) {
			bossBar.removeAll();
		}

		bossBar = Bukkit.createBossBar(
				formatBossbarText(currentArena, timeRemaining),
				BarColor.GREEN,
				BarStyle.SOLID
		);
		bossBar.setProgress(1.0);
		updateBossBar();
	}

	private String formatBossbarText(String arena, int seconds) {
		int minutes = seconds / 60;
		int remainingSeconds = seconds % 60;
		return ChatColor.translateAlternateColorCodes('&', "&7Current Arena: &6" + arena + " &8| &7Next Arena: &6" + minutes + "m " + remainingSeconds + "s");
	}

	public void updateBossBar() {
		if (!plugin.getConfig().getBoolean("boss-bar", true)) {
			if (bossBar != null) {
				bossBar.removeAll();
			}
			return;
		}

		currentArena = plugin.getArenaManager().getCurrentArenaName();
		bossBar.setTitle(formatBossbarText(currentArena, timeRemaining));
		bossBar.setProgress(Math.max(0, (double) timeRemaining / (plugin.getConfig().getInt("arena-time") * 60)));

		if (timeRemaining <= 10) {
			bossBar.setColor(BarColor.RED);
		} else if (timeRemaining <= 30) {
			bossBar.setColor(BarColor.YELLOW);
		} else {
			bossBar.setColor(BarColor.GREEN);
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			bossBar.addPlayer(player);
		}
	}

	public void setTimeRemaining(int seconds) {
		this.timeRemaining = seconds;
		updateBossBar();
	}

	public void resetBossBar() {
		this.timeRemaining = plugin.getConfig().getInt("arena-time") * 60;
		updateBossBar();
	}

	public void removePlayer(Player player) {
		if (bossBar != null) {
			bossBar.removePlayer(player);
		}
	}

	public void removeBossBar() {
		if (bossBar != null) {
			for (Player player : bossBar.getPlayers()) {
				bossBar.removePlayer(player);
			}
			bossBar.setVisible(false);
			Bukkit.getLogger().info("[DEBUG] BossBar successfully removed.");
		}
	}

}
