package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreBoardManager {

	private final NerKubKnockBackFFA plugin;

	private Scoreboard scoreboard;
	private Objective objective;
	private Map<Integer, String> scoreBoardLines;
	private String title;

	public ScoreBoardManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		this.scoreBoardLines = new HashMap<>();
		loadScoreboard(); // Načti scoreboard při inicializaci
	}

	private void loadScoreboard() {
		// Načti název scoreboardu
		title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "Default Title"));

		// Načti řádky scoreboardu
		scoreBoardLines.clear(); // Vymazání předchozích řádků
		for (String key : plugin.getConfig().getConfigurationSection("scoreboard.lines").getKeys(false)) {
			int lineNumber = Integer.parseInt(key);
			String lineText = ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("scoreboard.lines." + key));
			scoreBoardLines.put(lineNumber, lineText);
			// Ladicí výstup
			Bukkit.getLogger().info("Načten řádek pro scoreboard: " + lineNumber + ": " + lineText);
		}
	}

	public void updateScoreboard(Player player) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = manager.getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("gameInfo", "dummy", title);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		// Aktualizuj řádky scoreboardu
		for (Map.Entry<Integer, String> entry : scoreBoardLines.entrySet()) {
			String line = entry.getValue();
			// Nahrazení placeholderu %currentArena%
			if (line.contains("%currentarena%")) {
				String currentArena = plugin.getArenaManager().getCurrentArena();
				line = line.replace("%currentarena%", currentArena != null ? currentArena : "N/A");
			}
			Team team = scoreboard.registerNewTeam("line" + entry.getKey());
			team.addEntry(ChatColor.values()[entry.getKey()] + "");  // Barvy, aby každý tým měl unikátní řádek
			team.setPrefix(line);  // Zobrazí celý text jako prefix

			objective.getScore(ChatColor.values()[entry.getKey()] + "").setScore(entry.getKey());
		}

		// Přiřaď scoreboard hráči
		player.setScoreboard(scoreboard);
	}

	public void reloadScoreboard() {
		loadScoreboard(); // Obnoví scoreboard ze souboru

		// Zajistí okamžité změny ve scoreboardu
		for (Player player : Bukkit.getOnlinePlayers()) {
			updateScoreboard(player);
		}
	}

	public void startScoreboardUpdater(Player player) {
		new ScoreboardUpdater(plugin, player).runTaskTimer(plugin, 0, 20); // Aktualizuj každou sekundu (20 ticků)
	}
}
