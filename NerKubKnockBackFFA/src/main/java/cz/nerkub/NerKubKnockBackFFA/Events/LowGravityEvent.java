package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
	private final int duration;
	private final int strength;
	private final String startMessage;
	private final String endMessage;

	private final Set<UUID> affectedPlayers = new HashSet<>();
	private boolean eventActive = true; // Flag pro aktivní event

	public LowGravityEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = plugin.getArenaManager();

		FileConfiguration config = plugin.getEvents().getConfig();
		this.duration = config.getInt("event-settings.event-duration", 60);
		this.strength = config.getInt("events.low-gravity.strength", 2);
		this.startMessage = config.getString("events.low-gravity.message-start", "&c🏋️‍♂️ Gravity has increased! Jumping is difficult!");
		this.endMessage = config.getString("events.low-gravity.message-end", "&a🏋️‍♂️ Gravity is back to normal!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

		// Aplikujeme efekt všem hráčům, kteří nejsou v safezóne
		applyGravityEffectToAll();

		// Po uplynutí trvání eventu odstraníme efekt
		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false; // Deaktivace eventu
				removeGravityEffect();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null); // Ukončení eventu
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	public void applyGravityEffect(Player player) {
		// Aplikuj efekt pouze, pokud hráč není v safezóne
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
		player.removePotionEffect(PotionEffectType.JUMP);
		player.removePotionEffect(PotionEffectType.SLOW_FALLING);

		// Nastavení efektu: nižší skok (s indexem strength) a slow falling
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
			// Odebereme hráče z aktivního eventu v CustomEventManageru
			plugin.getCustomEventManager().removePlayerFromEvent(playerId, "LowGravity");
		}
		affectedPlayers.clear();
	}

	// Pokud hráč opustí safezónu během eventu, aplikuje se na něj efekt
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		// Pokud event ještě běží, hráč není již ovlivněn a opustil safezónu, aplikuj efekt
		if (eventActive && !affectedPlayers.contains(player.getUniqueId()) &&
				!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
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
