package cz.nerkub.NerKubKnockBackFFA.Expansions;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DeathsMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Managers.DatabaseManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.PlayerStats;
import cz.nerkub.NerKubKnockBackFFA.Managers.PlayerStatsManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.RankManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KnockBackPlaceholderExpansion extends PlaceholderExpansion {

	private final NerKubKnockBackFFA plugin;
	private final KillStreakMap killStreakMap;
	private final DatabaseManager databaseManager;

	public KnockBackPlaceholderExpansion(NerKubKnockBackFFA plugin, KillStreakMap killStreakMap, DatabaseManager databaseManager) {
		this.plugin = plugin;
		this.killStreakMap = killStreakMap;
		this.databaseManager = databaseManager;
	}

	@Override
	public @NotNull String getIdentifier() {
		return "knbffa";
	}

	@Override
	public @NotNull String getAuthor() {
		return "Nergl02";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, String params) {
		// Kontrola, zda je hráč null
		if (player == null || player.getName() == null) {
			return "N/A";
		}

		// 📌 Získání statistik hráče z MySQL
		PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
		if (stats == null) {
			return "0"; // Pokud hráč v databázi neexistuje, vrátí 0
		}

		// 📌 Implementace jednotlivých placeholderů
		switch (params.toLowerCase()) {
			case "currentarena":
				String arenaName = plugin.getArenaManager().getCurrentArenaName();
				return (arenaName != null) ? arenaName : "N/A";

			case "nextarenain":
				return plugin.formatTime(plugin.getTimeRemaining());

			case "killstreak":
				return String.valueOf(killStreakMap.getInt(player.getUniqueId()));

			case "kills":
				return String.valueOf(stats.getKills());

			case "deaths":
				return String.valueOf(stats.getDeaths());

			case "kd":
				return stats.getDeaths() > 0 ? String.format("%.2f", (double) stats.getKills() / stats.getDeaths()) : "∞";

			case "rank":
				return ChatColor.translateAlternateColorCodes('&', stats.getRank());

			case "elo":
				return String.valueOf(stats.getElo());

			case "maxkillstreak":
				return String.valueOf(stats.getMaxKillstreak());

			case "coins":
				return String.valueOf(stats.getCoins());

			default:
				return null; // Pokud placeholder neexistuje, vrátí null
		}
	}

}
