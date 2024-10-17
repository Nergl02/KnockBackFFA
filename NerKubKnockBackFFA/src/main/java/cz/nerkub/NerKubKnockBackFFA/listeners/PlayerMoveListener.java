package cz.nerkub.NerKubKnockBackFFA.listeners;

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
	private PlayerDamageListener damageListener;
	private final Random random;


	public PlayerMoveListener(NerKubKnockBackFFA plugin, Random damageListener) {
		this.plugin = plugin;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Player damager = damageListener.getDamager(player);
		Location spawn = player.getWorld().getSpawnLocation();

		if (player.getLocation().getY() <= plugin.getConfig().getInt("death-height")) {
			if (player.getLastDamageCause().getEntity() instanceof Player) {

				if (damageListener.getDamager(player).equals(player)) {
					Set<String> keys = plugin.getMessages().getConfig().getConfigurationSection("kill-messages").getKeys(false);
					List<String> keyList = new ArrayList<>(keys);
					String randomKey = keyList.get(random.nextInt(keyList.size()));

					Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getMessages().getConfig().getString("prefix") +
									plugin.getMessages().getConfig().getString("kill-messages." + randomKey)
											.replace("%player1%", damager.getDisplayName())
											.replace("%player2%", player.getDisplayName())));

					// Přidání Ender Pearl killerovi
					damager.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
					damageListener.removeDamager(player);
					// Teleportace hráče na spawn
					player.teleport(spawn);
				}
				player.teleport(spawn);
			}
		}

	}


}
