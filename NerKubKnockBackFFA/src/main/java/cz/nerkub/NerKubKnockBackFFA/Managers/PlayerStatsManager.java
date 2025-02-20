package cz.nerkub.NerKubKnockBackFFA.Managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {
	private final DatabaseManager databaseManager;
	private final Map<UUID, PlayerStats> statsCache;

	public PlayerStatsManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		this.statsCache = new HashMap<>();
	}

	public PlayerStats getStats(UUID uuid) {
		if (statsCache.containsKey(uuid)) {
			return statsCache.get(uuid);
		}

		PlayerStats stats = databaseManager.getPlayerStats(uuid.toString());
		if (stats != null) {
			statsCache.put(uuid, stats);
		}
		return stats;
	}

	public void saveStats(UUID uuid) {
		if (statsCache.containsKey(uuid)) {
			databaseManager.savePlayerStats(statsCache.get(uuid));
		}
	}

	public void updateStats(UUID uuid, int kills, int deaths, int killstreak, int elo, int coins, String rank) {
		PlayerStats stats = getStats(uuid);
		if (stats != null) {
			stats.setKills(stats.getKills() + kills);
			stats.setDeaths(stats.getDeaths() + deaths);
			stats.setMaxKillstreak(Math.max(stats.getMaxKillstreak(), killstreak));
			stats.setElo(elo);
			stats.setCoins(coins);
			stats.setRank(rank);
			saveStats(uuid);
		}
	}
}
