package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerSwapperListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;

	public PlayerSwapperListener(NerKubKnockBackFFA plugin, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
	}

	@EventHandler
	public void onPlayerSwap(EntityDamageByEntityEvent event) {

		if (event.getEntity() instanceof Player) {
			if (event.getDamager() instanceof Snowball) {
				Snowball snowball = (Snowball) event.getDamager();
				Player victim = (Player) event.getEntity();
				Player damager = (Player) snowball.getShooter();

				Location victimLoc = victim.getLocation();
				Location damagerLoc = damager.getLocation();

				damager.teleport(victimLoc);
				victim.teleport(damagerLoc);

				damager.playSound(damager.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
				victim.playSound(damager.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

				damagerMap.putDamager(victim.getUniqueId(), damager.getUniqueId());
			}
		}

	}

}
