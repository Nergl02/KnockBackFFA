package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RankManager {

	private final NerKubKnockBackFFA plugin;

	public RankManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public int getPlayerElo(Player player) {
		return plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".elo", 0);
	}

	public String getRankFromElo(Player player) {
		int elo = getPlayerElo(player);

		// Debug output
		Bukkit.getLogger().info("ELO for " + player.getDisplayName() + ": " + elo);

		// Pokud je ELO menší než 100, vracíme "Unranked"
		if (elo < 100) {
			return "Unranked";
		}

		// Procházení všech ranků v konfiguračním souboru
		for (String rankKey : plugin.getConfig().getConfigurationSection("ranks").getKeys(false)) {
			int min = plugin.getConfig().getInt("ranks." + rankKey + ".min");
			int max = plugin.getConfig().getInt("ranks." + rankKey + ".max");

			// Debug output
			Bukkit.getLogger().info("Checking rank: " + rankKey + " with range: " + min + " - " + max);

			if (elo >= min && elo <= max) {
				return plugin.getConfig().getString("ranks." + rankKey + ".display");
			}
		}

		return "no ranks"; // Pokud ELO neodpovídá žádnému ranku
	}

	public String savePlayerRank(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		String oldRank = plugin.getPlayers().getConfig().getString(player.getDisplayName() + ".rank", "Unranked");
		String newRank = getRankFromElo(player);

		// Uložení nového ranku do configu
		plugin.getPlayers().getConfig().set(player.getDisplayName() + ".rank", newRank);
		plugin.getPlayers().saveConfig();

		// Kontrola, zda došlo k povýšení nebo ponížení
		if (!oldRank.equals(newRank)) {
			if (isRankHigher(newRank, oldRank)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("rank.promoted").replace("%rank%", newRank)));
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("rank.demoted").replace("%rank%", newRank)));
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			}
		}

		return newRank;
	}

	// Metoda pro kontrolu, zda je nový rank vyšší než starý
	private boolean isRankHigher(String newRank, String oldRank) {
		// Vytvoř seznam ranků seřazených podle úrovně
		List<String> ranksList = Arrays.asList("Unranked", "novice", "apprentice", "adept", "warrior", "champion", "gladiator", "hero", "master", "legend", "mythic");

		int newRankIndex = ranksList.indexOf(newRank.toLowerCase());
		int oldRankIndex = ranksList.indexOf(oldRank.toLowerCase());

		return newRankIndex > oldRankIndex; // Vrátí true, pokud je nový rank vyšší
	}
}
