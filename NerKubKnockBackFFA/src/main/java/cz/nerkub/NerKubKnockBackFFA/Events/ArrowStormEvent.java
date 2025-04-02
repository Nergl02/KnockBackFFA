package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
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

	private boolean eventActive = true;
	private Location arenaSpawn;

	public ArrowStormEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = arenaManager;

		if (!plugin.getEvents().getConfig().getBoolean("events.arrow-storm.enabled", true)) return;

		this.arenaSpawn = arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName());
		if (arenaSpawn == null) {
			plugin.getLogger().warning("⚠️ ArrowStormEvent aborted: Arena spawn is null.");
			return;
		}

		int arrowCount = plugin.getEvents().getConfig().getInt("events.arrow-storm.arrow-count", 50);
		int arrowSpawnRadius = plugin.getEvents().getConfig().getInt("events.arrow-storm.arrow-spawn-radius", 30);
		int duration = plugin.getEvents().getConfig().getInt("event-settings.event-duration", 60);
		String startMessage = plugin.getEvents().getConfig().getString("events.arrow-storm.message-start", "&c⚡ Arrow Storm has begun!");
		String endMessage = plugin.getEvents().getConfig().getString("events.arrow-storm.message-end", "&aArrow Storm is over!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));
		Bukkit.getPluginManager().registerEvents(this, plugin);

		int interval = Math.max((duration * 20) / arrowCount, 1);

		new BukkitRunnable() {
			int arrowsLeft = arrowCount;

			@Override
			public void run() {
				if (arrowsLeft-- <= 0 || !eventActive) {
					cancel();
					return;
				}
				spawnArrow(arrowSpawnRadius);
			}
		}.runTaskTimer(plugin, 0L, interval);

		new BukkitRunnable() {
			@Override
			public void run() {
				removeAllArrows();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				eventActive = false;
				plugin.getCustomEventManager().setCurrentEvent(null);
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	private void spawnArrow(int spawnRadius) {
		double arrowSpawnY = arenaSpawn.getY() + 15 + random.nextInt(10);
		Location location;

		do {
			double xOffset = (random.nextDouble() * (spawnRadius * 2)) - spawnRadius;
			double zOffset = (random.nextDouble() * (spawnRadius * 2)) - spawnRadius;
			location = new Location(arenaSpawn.getWorld(), arenaSpawn.getX() + xOffset, arrowSpawnY, arenaSpawn.getZ() + zOffset);
		} while (safeZoneManager.isInSafeZone(location, arenaSpawn));

		Arrow arrow = location.getWorld().spawnArrow(location, new Vector(0, -1, 0), 1.5f, 12);
		arrow.setGravity(true);
		arrow.setMetadata("arrowstorm", new FixedMetadataValue(plugin, true));
	}

	private void removeAllArrows() {
		Bukkit.getWorlds().forEach(world ->
				world.getEntitiesByClass(Arrow.class).forEach(arrow -> {
					if (arrow.hasMetadata("arrowstorm")) {
						arrow.remove();
					}
				}));
	}

	@EventHandler
	public void onArrowLand(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Arrow arrow)) return;
		if (!arrow.hasMetadata("arrowstorm")) return;

		Location hit = arrow.getLocation();
		arrow.remove();

		float explosionRadius = (float) plugin.getEvents().getConfig().getDouble("events.arrow-storm.explosion-radius", 2.0);
		double explosionPower = plugin.getEvents().getConfig().getDouble("events.arrow-storm.explosion-power", 3.0);

		hit.getWorld().createExplosion(hit, explosionRadius, false, false);

		hit.getWorld().getNearbyEntities(hit, 3, 3, 3).forEach(entity -> {
			if (entity instanceof Player player) {
				Vector knockback = player.getLocation().toVector().subtract(hit.toVector()).normalize().multiply(explosionPower);
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
