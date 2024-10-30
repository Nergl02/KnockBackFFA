package cz.nerkub.NerKubKnockBackFFA.Expansions;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DeathsMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Managers.RankManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KnockBackPlaceholderExpansion extends PlaceholderExpansion {

	private final NerKubKnockBackFFA plugin;
	private final KillStreakMap killStreakMap;
	private final KillsMap killsMap;
	private final DeathsMap deathsMap;
	private final RankManager rankManager;

	public KnockBackPlaceholderExpansion(NerKubKnockBackFFA plugin, KillStreakMap killStreakMap, KillsMap killsMap, DeathsMap deathsMap, RankManager rankManager) {
		this.plugin = plugin;
		this.killStreakMap = killStreakMap;
		this.killsMap = killsMap;
		this.deathsMap = deathsMap;
		this.rankManager = rankManager;
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
		if (player == null) {
			return ""; // Vrátí prázdný řetězec, pokud je hráč null
		}

		if (params.equals("currentarena")) {
			return plugin.getArenaManager().getCurrentArena() != null
					? plugin.getArenaManager().getCurrentArena()
					: "N/A"; // Může vrátit "N/A" nebo jinou výchozí hodnotu
		}

		if (params.equals("nextarenain")) {
			return plugin.formatTime(plugin.getTimeRemaining());
		}

		if (params.equals("killstreak")) {
			Integer killStreak = killStreakMap.getInt(player.getUniqueId());
			return killStreak != null ? killStreak.toString() : "0"; // Ošetření, pokud je killstreak null
		}

		if (params.equals("kills")) {
			// Načti kills z databáze
			Integer kills = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".kills");
			return kills != null ? kills.toString() : "0"; // Pokud je null, vrať "0"
		}

		if (params.equals("deaths")) {
			// Načti deaths z databáze
			Integer deaths = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".deaths");
			return deaths != null ? deaths.toString() : "0"; // Pokud je null, vrať "0"
		}

		if (params.equals("kd")) {
			// Načti kills a deaths
			Integer kills = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".kills");
			Integer deaths = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".deaths");

			// Ověř, zda jsou kills a deaths null
			if (kills == null) {
				kills = 0; // Pokud kills je null, nastav na 0
			}
			if (deaths == null || deaths == 0) {
				return kills > 0 ? "∞" : "0"; // Pokud jsou úmrtí 0 a zabití > 0, vrať "∞"; jinak "0"
			}

			double kdRatio = (double) kills / deaths; // Vypočti KD poměr
			return String.format("%.2f", kdRatio); // Formátování na dvě desetinná místa
		}

		if (params.equals("rank")) {
			return plugin.getPlayers().getConfig().getString(player.getDisplayName() + ".rank");
		}

		if (params.equals("elo")) {
			return plugin.getPlayers().getConfig().getString(player.getDisplayName() + ".elo");
		}

		if (params.equals("maxkillstreak")) {
			return String.valueOf(plugin.getPlayers().getConfig().getInt(player.getDisplayName() + (".max-kill-streak")));
		}

		return null; // Vrátí null, pokud placeholder neexistuje
	}

}
