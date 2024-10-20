package cz.nerkub.NerKubKnockBackFFA.listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerMoveListener implements Listener {

	private NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;
	private final Random random;


	public PlayerMoveListener(NerKubKnockBackFFA plugin, Random random, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location spawn = player.getWorld().getSpawnLocation();

		if (player.getLocation().getY() <= plugin.getConfig().getInt("death-height")) {
			if (!damagerMap.hasDamager(player.getUniqueId())) {
				player.teleport(spawn);
			}
			UUID damager = damagerMap.getDamager(player.getUniqueId());
			Set<String> keys = plugin.getMessages().getConfig().getConfigurationSection("kill-messages").getKeys(false);
			List<String> keyList = new ArrayList<>(keys);
			String randomKey = keyList.get(random.nextInt(keyList.size()));

			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("kill-messages." + randomKey)
									.replace("%player1%", Bukkit.getPlayer(damager).getDisplayName())
									.replace("%player2%", player.getDisplayName())));

			// Přidání Ender Pearl killerovi
			Bukkit.getPlayer(damager).getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			damagerMap.removeDamager(player.getUniqueId());
			// Teleportace hráče na spawn
			player.teleport(spawn);
		}
	}

}

