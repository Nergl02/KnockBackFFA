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
		// Ověř, zda poškozený je hráč
		if (!(event.getEntity() instanceof Player)) return;
		Player victim = (Player) event.getEntity();

		// 🔹 Útok hráčem
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			damagerMap.putDamager(victim.getUniqueId(), damager.getUniqueId());
		}

		// 🔹 Útok šípem
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				event.setDamage(0); // Vypnutí poškození
				damagerMap.putDamager(victim.getUniqueId(), shooter.getUniqueId());
			}
		}

		// 🔹 Útok EnderPearlou
		if (event.getDamager() instanceof EnderPearl) {
			event.setDamage(0); // EnderPearla neubližuje
		}
	}


}

