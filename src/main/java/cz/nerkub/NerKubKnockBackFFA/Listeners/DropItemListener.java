package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class DropItemListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public DropItemListener(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}


	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (arenaManager.isPlayerInArena(player)) {
			if (player.getGameMode() == GameMode.SURVIVAL){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPalayerPickUpItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();


		if (arenaManager.isPlayerInArena(player)) {
			if (player.getGameMode() == GameMode.SURVIVAL){
				event.setCancelled(true);
			}
		}
	}
}
