package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LowGravityEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;

	private final Set<UUID> affectedPlayers = new HashSet<>();
	private boolean eventActive = true;

	public LowGravityEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = plugin.getArenaManager();

		FileConfiguration config = plugin.getEvents().getConfig();
		int duration = config.getInt("event-settings.event-duration", 60);
		String startMessage = config.getString("events.low-gravity.message-start", "&c🏋️‍♂️ Gravity has increased!");
		String endMessage = config.getString("events.low-gravity.message-end", "&a🏋️‍♂️ Gravity is back to normal!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

		applyGravityEffectToAll();

		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false;
				removeGravityEffect();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null);
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	public void applyGravityEffect(Player player) {
		if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
			giveGravityEffect(player);
		}
	}

	public void applyGravityEffectToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
				giveGravityEffect(player);
			}
		}
	}

	private void giveGravityEffect(Player player) {
		FileConfiguration config = plugin.getEvents().getConfig();
		int duration = config.getInt("event-settings.event-duration", 60);
		int strength = config.getInt("events.low-gravity.strength", 2);

		player.removePotionEffect(PotionEffectType.JUMP);
		player.removePotionEffect(PotionEffectType.SLOW_FALLING);

		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration * 20, strength, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration * 20, 0, false, false));

		affectedPlayers.add(player.getUniqueId());
	}

	private void removeGravityEffect() {
		for (UUID playerId : affectedPlayers) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				player.removePotionEffect(PotionEffectType.JUMP);
				player.removePotionEffect(PotionEffectType.SLOW_FALLING);
			}
			plugin.getCustomEventManager().removePlayerFromEvent(playerId, "LowGravity");
		}
		affectedPlayers.clear();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		if (!eventActive) return;

		if (!affectedPlayers.contains(playerId)
				&& !safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
			giveGravityEffect(player);
		}
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
