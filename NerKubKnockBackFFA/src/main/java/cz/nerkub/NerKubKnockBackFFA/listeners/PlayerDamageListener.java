package cz.nerkub.NerKubKnockBackFFA.listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

	private NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;

	public PlayerDamageListener(NerKubKnockBackFFA plugin, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
	}


	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		Player victim = (Player) event.getEntity();
		Player damager = (Player) event.getDamager();
		// Zkontroluj, jestli útočník je také hráč
		if (event.getDamager() instanceof Player) {
			// Uložit útočníka do mapy
			damagerMap.putDamager(victim.getUniqueId(), damager.getUniqueId());
			damager.sendMessage("test");
			victim.sendMessage("victim");
		}
	}

}

