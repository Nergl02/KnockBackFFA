package cz.nerkub.NerKubKnockBackFFA.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropItemListener implements Listener {


	@EventHandler
	public void onPlayerDropItem (PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
}
