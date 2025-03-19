package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ArrowStormEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;
	private final Random random = new Random();

	private int arrowCount;
	private double explosionRadius;
	private double explosionPower;
	private int arrowSpawnRadius;
	private String startMessage;
	private String endMessage;
	private int duration;

	private boolean eventActive = true; // Přidáno pro kontrolu ukončení eventu

	public ArrowStormEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = arenaManager;
		loadConfig();

		if (!plugin.getEvents().getConfig().getBoolean("events.arrow-storm.enabled", true)) {
			return;
		}

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));
		Bukkit.getPluginManager().registerEvents(this, plugin);

		Location arenaCenter = plugin.getArenaManager().getArenaSpawn(plugin.getArenaManager().getCurrentArenaName());

		// 📌 Vypočítáme interval mezi šípy
		int interval = Math.max((duration * 20) / arrowCount, 1); // Zajistíme, že interval nebude 0

		new BukkitRunnable() {
			int arrowsLeft = arrowCount;

			@Override
			public void run() {
				if (arrowsLeft <= 0 || !eventActive) {
					cancel();
					return;
				}

				double xOffset = (random.nextDouble() * (arrowSpawnRadius * 2)) - arrowSpawnRadius;
				double zOffset = (random.nextDouble() * (arrowSpawnRadius * 2)) - arrowSpawnRadius;
				Location spawnLocation = arenaCenter.clone().add(xOffset, 15, zOffset);

				spawnArrow(spawnLocation);
				arrowsLeft--;
			}
		}.runTaskTimer(plugin, 0L, interval); // Použitý **vypočítaný interval**

		// 📌 Automatické ukončení eventu
		new BukkitRunnable() {
			@Override
			public void run() {
				removeAllArrows();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				eventActive = false; // Zastaví spawnování šípů
				plugin.getCustomEventManager().setCurrentEvent(null);
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	private void loadConfig() {
		FileConfiguration config = plugin.getEvents().getConfig();
		this.arrowCount = config.getInt("events.arrow-storm.arrow-count", 50);
		this.explosionRadius = config.getDouble("events.arrow-storm.explosion-radius", 2.0);
		this.explosionPower = config.getDouble("events.arrow-storm.explosion-power", 3.0);
		this.arrowSpawnRadius = config.getInt("events.arrow-storm.arrow-spawn-radius", 30);
		this.startMessage = config.getString("events.arrow-storm.message-start", "&c⚡ Arrow Storm has begun! Watch out for falling arrows!");
		this.endMessage = config.getString("events.arrow-storm.message-end", "&aArrow Storm is over!");
		this.duration = config.getInt("event-settings.event-duration", 60);
	}

	private void spawnArrow(Location location) {
		Location arenaSpawn = plugin.getArenaManager().getArenaSpawn(plugin.getArenaManager().getCurrentArenaName());

		// Dynamicky nastavíme výšku podle arény (např. +15 až +25 bloků)
		double arrowSpawnY = arenaSpawn.getY() + 15 + random.nextInt(10); // 15 až 25 bloků nad arénou

		// Znovu generujeme souřadnice, pokud jsou v safezóně
		do {
			double xOffset = (random.nextDouble() * (arrowSpawnRadius * 2)) - arrowSpawnRadius;
			double zOffset = (random.nextDouble() * (arrowSpawnRadius * 2)) - arrowSpawnRadius;
			location = new Location(arenaSpawn.getWorld(), arenaSpawn.getX() + xOffset, arrowSpawnY, arenaSpawn.getZ() + zOffset);
		} while (safeZoneManager.isInSafeZone(location, arenaSpawn));

		// Když už není v safezóně, spawneme šíp
		Arrow arrow = location.getWorld().spawnArrow(location, new Vector(0, -1, 0), 1.5f, 12);
		arrow.setGravity(true);
		arrow.setMetadata("arrowstorm", new FixedMetadataValue(plugin, true)); // Označení eventových šípů
	}



	private void removeAllArrows() {
		Bukkit.getWorlds().forEach(world ->
				world.getEntitiesByClass(Arrow.class).forEach(Arrow::remove)
		);
	}

	@EventHandler
	public void onArrowLand(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Arrow arrow)) return;

		// Pokud šíp nemá metadata "arrowstorm", není součástí eventu → neexploduje
		if (!arrow.hasMetadata("arrowstorm")) return;

		Location hitLocation = arrow.getLocation();
		arrow.remove(); // Odstranění šípu po dopadu

		// Výbuch pouze u šípů z eventu
		hitLocation.getWorld().createExplosion(hitLocation, (float) explosionRadius, false, false);

		// Knockback pro hráče v okolí
		hitLocation.getWorld().getNearbyEntities(hitLocation, 3, 3, 3).forEach(entity -> {
			if (entity instanceof Player player) {
				Vector knockback = player.getLocation().toVector().subtract(hitLocation.toVector()).normalize().multiply(explosionPower);
				player.setVelocity(knockback);
			}
		});
	}


	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
