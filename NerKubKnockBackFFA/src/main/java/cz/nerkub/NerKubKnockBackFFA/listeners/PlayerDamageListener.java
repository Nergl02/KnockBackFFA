package cz.nerkub.NerKubKnockBackFFA.listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class PlayerDamageListener implements Listener {

	private NerKubKnockBackFFA plugin;
	private final Random random;

	public PlayerDamageListener(NerKubKnockBackFFA plugin, Random random) {
		this.plugin = plugin;
		this.random = random;
	}

	private final Map<Player, Player> damagerMap = new HashMap<>();

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player victim) {
			Location spawn = victim.getWorld().getSpawnLocation();
			// Zkontroluj, jestli útočník je také hráč
			if (event.getDamager() instanceof Player damager) {
				// Uložit útočníka do mapy
				damagerMap.put(victim, damager);

			}
		}
	}

	public Player getDamager(Player victim) {
		return damagerMap.get(victim);
	}

	public Player removeDamager (Player victim) {
		return damagerMap.remove(victim);
	}

}

