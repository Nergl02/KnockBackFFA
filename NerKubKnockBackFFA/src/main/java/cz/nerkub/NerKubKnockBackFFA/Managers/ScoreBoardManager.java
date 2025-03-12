package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreBoardManager {

	private final NerKubKnockBackFFA plugin;
	private final Map<Integer, String> scoreBoardLines;
	private final Map<Player, ScoreBoardUpdater> scoreboardUpdaters; // Mapa pro sledování updaters

	private String title;

	public ScoreBoardManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		this.scoreBoardLines = new HashMap<>();
		this.scoreboardUpdaters = new HashMap<>(); // Inicializace mapy
		loadScoreboard(); // Načti scoreboard při inicializaci
	}

	private void loadScoreboard() {
		title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "Default Title"));
		scoreBoardLines.clear(); // Vymazání předchozích řádků
		for (String key : plugin.getConfig().getConfigurationSection("scoreboard.lines").getKeys(false)) {
			int lineNumber = Integer.parseInt(key);
			String lineText = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.lines." + key));
			scoreBoardLines.put(lineNumber, lineText);
		}
	}

	public void updateScoreboard(Player player) {
		Scoreboard scoreboard = player.getScoreboard();

		if (scoreboard == null || scoreboard.getObjective("gameInfo") == null) {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			scoreboard = manager.getNewScoreboard();
			Objective objective = scoreboard.registerNewObjective("gameInfo", "dummy", title);
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		Objective objective = scoreboard.getObjective("gameInfo");
		for (Map.Entry<Integer, String> entry : scoreBoardLines.entrySet()) {
			String line = PlaceholderAPI.setPlaceholders(player, entry.getValue());
			Team team = scoreboard.getTeam("line" + entry.getKey());

			if (team == null) {
				team = scoreboard.registerNewTeam("line" + entry.getKey());
				team.addEntry(ChatColor.values()[entry.getKey()] + "");
				objective.getScore(ChatColor.values()[entry.getKey()] + "").setScore(entry.getKey());
			}
			team.setPrefix(line);
		}

		player.setScoreboard(scoreboard);
	}


	public void reloadScoreboard() {
		loadScoreboard();
		for (Player player : Bukkit.getOnlinePlayers()) {
			updateScoreboard(player);
		}
	}

	public void startScoreboardUpdater(Player player) {
		ScoreBoardUpdater updater = new ScoreBoardUpdater(plugin, player);
		updater.runTaskTimer(plugin, 0, 20); // Aktualizace každou sekundu

		// Přidejte updater do mapy pro pozdější zastavení
		scoreboardUpdaters.put(player, updater);
	}

	public void stopScoreboardUpdater(Player player) {
		ScoreBoardUpdater updater = scoreboardUpdaters.remove(player); // Odeber a vrať updater
		if (updater != null) {
			updater.cancel(); // Zruš updater pro zastavení aktualizace
		}
	}

	public void removeScoreboard(Player player) {
		stopScoreboardUpdater(player); // Zastavíme jakýkoliv probíhající updater

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		if (manager != null) {
			player.setScoreboard(manager.getNewScoreboard());
		}
	}
}
