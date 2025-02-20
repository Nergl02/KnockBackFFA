package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class ExplodingChickListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;

	public ExplodingChickListener(NerKubKnockBackFFA plugin, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
	}

	@EventHandler
	public void onEggHit(ProjectileHitEvent event){
		if (!(event.getEntity() instanceof Egg)) return;
		if (!(event.getEntity().getShooter() instanceof Player)) return;

		Egg egg = (Egg) event.getEntity();
		Player player = (Player) egg.getShooter();

		// Vytvoření kuřete na místě dopadu
		Location hitLocation = egg.getLocation();
		Chicken chick = (Chicken) hitLocation.getWorld().spawnEntity(hitLocation, EntityType.CHICKEN);
		chick.setCustomName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("exploding-chick.display-name")));
		chick.setCustomNameVisible(true);
		chick.setInvulnerable(false);
		chick.setSilent(true);

		Player target = hitLocation.getWorld().getNearbyEntities(hitLocation, 10, 10, 10).stream()
				.filter(entity -> entity instanceof Player && !entity.equals(player))
				.map(entity -> (Player) entity)
				.findFirst().orElse(null);

		// Sekundy odpočítávání výbuchu
		// Výbuch po odpočítávání
		new BukkitRunnable() {
			int countdown = plugin.getItems().getConfig().getInt("exploding-chick.explosion-delay");
			int radius = plugin.getItems().getConfig().getInt("exploding-chick.explosion-radius");
			int power = plugin.getItems().getConfig().getInt("exploding-chick.power");

			@Override
			public void run() {
				if (!chick.isValid() || !target.isOnline()) {
					cancel();
					return;
				}

				// Sleduj hráče jako creeper
				chick.getPathfinder().moveTo(target.getLocation(), 1.2);  // 1.2 = rychlost pohybu

				if (countdown <= 0) {
					// Výbuch
					Location explosionLocation = chick.getLocation();
					explosionLocation.getWorld().playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
					explosionLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, explosionLocation, 1);

					// Knockback hráčů v okolí
					explosionLocation.getWorld().getNearbyEntities(explosionLocation, radius, radius, radius).forEach(entity -> {
						if (entity instanceof Player) {
							Player nearbyPlayer = (Player) entity;
							Vector knockback = nearbyPlayer.getLocation().toVector().subtract(explosionLocation.toVector()).normalize().multiply(power);
							nearbyPlayer.setVelocity(knockback);
							damagerMap.putDamager(nearbyPlayer.getUniqueId(), player.getUniqueId());
						}
					});

					chick.remove();
					cancel();
				} else {
					// Aktualizace názvu kuřete s odpočtem
					chick.setCustomName(ChatColor.translateAlternateColorCodes('&',
							plugin.getItems().getConfig().getString("exploding-chick.exploding-info") + countdown + "s"));
					countdown--;
				}
			}
		}.runTaskTimer(plugin, 0L, 10L);  // 10L = rychlejší aktualizace
	}

	@EventHandler
	public void onChickenHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Chicken && event.getDamager() instanceof Player) {
			Chicken chick = (Chicken) event.getEntity();
			Player player = (Player) event.getDamager();

			Vector knockback = player.getLocation().getDirection().multiply(1.5).setY(0.5);
			chick.setVelocity(knockback);
			chick.getWorld().playSound(chick.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.0f);
		}
	}
}
