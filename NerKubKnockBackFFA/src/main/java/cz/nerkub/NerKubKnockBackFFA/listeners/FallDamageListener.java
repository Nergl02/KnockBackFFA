package cz.nerkub.NerKubKnockBackFFA.listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class FallDamageListener implements Listener {

	 private NerKubKnockBackFFA plugin;

	public FallDamageListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDamage (EntityDamageEvent event) {
		Player player = (Player) event.getEntity();

		if (event.getEntity() instanceof Player) {
			if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
				event.setCancelled(true);
			}
		}
	}
}
