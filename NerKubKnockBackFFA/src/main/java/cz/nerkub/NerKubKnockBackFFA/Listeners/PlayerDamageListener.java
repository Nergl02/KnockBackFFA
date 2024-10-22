package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerDamageListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;

	public PlayerDamageListener(NerKubKnockBackFFA plugin, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
	}


	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		// Zkontroluj, jestli útočník je také hráč
		if (event.getDamager() instanceof Player) {
			Player victim = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			// Uložit útočníka do mapy
			damagerMap.putDamager(victim.getUniqueId(), damager.getUniqueId());
		}
	}


	// Zrušení EnderPearl damage
	@EventHandler
	public void onPlayerDamageByEnderPearl (PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location targetLocation = event.getTo();
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			event.setCancelled(true);
			player.teleport(targetLocation);
		}
	}

	@EventHandler
	public void onPlayerDamageByArrow (EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager instanceof Arrow) {
			event.setDamage(0);
		}
	}


}

