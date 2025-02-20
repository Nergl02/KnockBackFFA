package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;


import java.util.*;

public class RankManager {

	private final NerKubKnockBackFFA plugin;
	private final Map<String, Rank> ranks = new LinkedHashMap<>();

	public RankManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		loadRanks();
	}

	public void loadRanks() {
		ranks.clear();
		ConfigurationSection section = plugin.getRanks().getConfig().getConfigurationSection("ranks");

		if (section != null) {
			for (String key : section.getKeys(false)) {
				String display = section.getString(key + ".display", key);
				display = ChatColor.translateAlternateColorCodes('&', display);
				int min = section.getInt(key + ".min", 0);
				int max = section.getInt(key + ".max", Integer.MAX_VALUE);
				ranks.put(key.toLowerCase(), new Rank(key, display, min, max));
			}
		}

	}

	public int getPlayerElo(Player player) {
		PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
		return (stats != null) ? stats.getElo() : 0;
	}

	// Určení ranku podle ELO
	public String getRankFromElo(Player player) {
		int elo = getPlayerElo(player);

		for (Rank rank : ranks.values()) {
			if (elo >= rank.getMin() && elo <= rank.getMax()) {
				return rank.getDisplay();
			}
		}

		return plugin.getRanks().getConfig().getString("default-rank.display"); // Pokud hráč nespadá do žádného ranku
	}


	public void savePlayerRank(Player player) {
		PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
		if (stats == null) return;

		String oldRank = stats.getRank() != null ? stats.getRank() : plugin.getRanks().getConfig().getString("default-rank.display");
		String newRank = getRankFromElo(player);

		if (!oldRank.equalsIgnoreCase(newRank)) {
			stats.setRank(newRank);
			plugin.getPlayerStatsManager().saveStats(player.getUniqueId());

			// Oznámení o změně ranku
			String prefix = plugin.getMessages().getConfig().getString("prefix");

			if (isRankHigher(newRank, oldRank)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("rank.promoted")
								.replace("%rank%", newRank)));
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("rank.demoted")
								.replace("%rank%", newRank)));
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			}
		}
	}

	private boolean isRankHigher(String newRank, String oldRank) {
		int newIndex = new ArrayList<>(ranks.keySet()).indexOf(newRank.toLowerCase());
		int oldIndex = new ArrayList<>(ranks.keySet()).indexOf(oldRank.toLowerCase());
		return newIndex > oldIndex;
	}
}
