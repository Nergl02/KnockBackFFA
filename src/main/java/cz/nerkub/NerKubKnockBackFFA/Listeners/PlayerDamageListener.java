package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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


	@EventHandler
	public void onPlayerDamageByArrow(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Entity damager = event.getDamager();

			if (damager instanceof Arrow) {
				Arrow arrow = (Arrow) damager;
				if (arrow.getShooter() instanceof Player) {
					Player shooter = (Player) arrow.getShooter();
					event.setDamage(0); // Nastaví poškození na nulu
					damagerMap.putDamager(player.getUniqueId(), shooter.getUniqueId()); // Přidá útočníka do mapy
				}
			}
			if (damager instanceof EnderPearl) {
				EnderPearl enderPearl = (EnderPearl) damager;
				event.setDamage(0);
			}
		}
	}


}

