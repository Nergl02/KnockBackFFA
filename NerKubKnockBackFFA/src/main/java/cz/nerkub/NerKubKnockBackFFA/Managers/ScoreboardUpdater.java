package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdater extends BukkitRunnable {

	private final NerKubKnockBackFFA plugin;
	private final Player player;

	public ScoreboardUpdater(NerKubKnockBackFFA plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public void run() {
		// Vykoná update scoreboardu každých 20 ticků
		plugin.getScoreBoardManager().updateScoreboard(player);
	}
}
