package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreBoardUpdater extends BukkitRunnable {

	private final NerKubKnockBackFFA plugin;
	private final Player player;

	public ScoreBoardUpdater(NerKubKnockBackFFA plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public void run() {
		if (!player.isOnline()) {
			cancel();
			return;
		}
		plugin.getScoreBoardManager().updateScoreboard(player);
	}

}
