package cz.nerkub.NerKubKnockBackFFA.listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class PlayerDeathListener implements Listener {


	private NerKubKnockBackFFA plugin;
	private final Random random;

	public PlayerDeathListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerDeath (PlayerDeathEvent event) {
		Player player = event.getEntity().getPlayer();
		Player killer = player.getKiller();

		Set<String> keys = plugin.getMessages().getConfig().getConfigurationSection("kill-messages").getKeys(false);
		List<String> keyList = new ArrayList<>(keys);

		String randomKey = keyList.get(random.nextInt(keyList.size()));

		if (killer != null) {
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") + plugin.getMessages().getConfig().getString("kill-messages." + randomKey)
							.replace("%player1%", killer.getDisplayName())
							.replace("%player2%", player.getDisplayName())));
		}

		player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
		killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		event.setDeathMessage(null);
	}

}
