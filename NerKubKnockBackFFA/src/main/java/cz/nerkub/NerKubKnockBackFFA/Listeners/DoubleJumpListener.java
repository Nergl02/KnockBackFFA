package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.UUID;

public class DoubleJumpListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private int cooldownTime;
	private double jumpPower;
	private final HashMap<UUID, Long> cooldowns = new HashMap<>();

	public DoubleJumpListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		reloadConfigValues();
	}

	public void reloadConfigValues() {
		this.cooldownTime = plugin.getConfig().getInt("doublejump.cooldown", 3);
		this.jumpPower = plugin.getConfig().getDouble("doublejump.power", 1.0);
	}

	@EventHandler
	public void onPlayerMore(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() != GameMode.CREATIVE) {
			player.setAllowFlight(true);
		}

	}

	@EventHandler
	public void onPlayerFlight(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE) return;

		event.setCancelled(true);

		if (cooldowns.containsKey(player.getUniqueId())) {
			long timeLeft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
			if (timeLeft > 0) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getMessages().getConfig().getString("doublejump.cooldown").replace("%timeleft%", String.valueOf(timeLeft))));
				return;
			}
		}

		// Skok s dynamickou silou
		player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(jumpPower));

		// Efekty
		player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10);
		player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);

		// Deaktivace letu (znovu povoleno při dopadu)
		player.setAllowFlight(false);

		// Nastavení cooldownu
		cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownTime * 1000));
	}

	}
