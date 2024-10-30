package cz.nerkub.NerKubKnockBackFFA.Listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropItemListener implements Listener {


	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.SURVIVAL) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMoveItem(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();

		if (player.getGameMode() == GameMode.SURVIVAL) {
			event.setCancelled(true);
		}
	}
}
