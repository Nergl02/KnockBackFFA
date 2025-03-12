package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LowGravityEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final int duration;
	private final int strength;
	private final String startMessage;
	private final String endMessage;

	public LowGravityEvent(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;

		FileConfiguration config = plugin.getEvents().getConfig();
		this.duration = config.getInt("event-settings.event-duration", 60);
		this.strength = config.getInt("events.low-gravity.strength", 2);
		this.startMessage = config.getString("events.low-gravity.message-start", "&c🏋️‍♂️ Gravity has increased! Jumping is difficult!");
		this.endMessage = config.getString("events.low-gravity.message-end", "&a🏋️‍♂️ Gravity is back to normal!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

		applyGravityEffect();

		new BukkitRunnable() {
			@Override
			public void run() {
				removeGravityEffect();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null); // Ukončení eventu
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	private void applyGravityEffect() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.removePotionEffect(PotionEffectType.SLOW_FALLING);

			// 🏋️‍♂️ Nastavení nižšího skoku (-2) a slow falling, aby pád nebyl trhaný
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration * 20, strength, false, false));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration * 20, 0, false, false));
		}
	}

	private void removeGravityEffect() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.removePotionEffect(PotionEffectType.SLOW_FALLING);
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
