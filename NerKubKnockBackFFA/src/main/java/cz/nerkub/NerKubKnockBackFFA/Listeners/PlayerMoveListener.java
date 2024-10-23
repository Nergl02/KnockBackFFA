package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerMoveListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;
	private final Random random;
	private final BuildBlockItem buildBlockItem;
	private final ArenaManager arenaManager;


	public PlayerMoveListener(NerKubKnockBackFFA plugin, Random random, DamagerMap damagerMap, BuildBlockItem buildBlockItem, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
		this.buildBlockItem = buildBlockItem;
		this.arenaManager = arenaManager;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getLocation().getY() <= plugin.getConfig().getInt("death-height")) {
			// POKUD NENÍ DAMAGER A HRÁČ SPADNE SÁM
			if (!damagerMap.hasDamager(player.getUniqueId())) {

				arenaManager.teleportPlayerToCurrentArena(player);

				player.getInventory().remove(new ItemStack(Material.ENDER_PEARL));
				player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
				player.getInventory().remove(new ItemStack(Material.ARROW));
				player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
				player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

				player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);

				return;
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
			if (Bukkit.getPlayer(damager).getInventory().contains(Material.ARROW)) {
				Bukkit.getPlayer(damager).getInventory().addItem(new ItemStack(Material.ARROW));
			} else {
				Bukkit.getPlayer(damager).getInventory().setItem(9, new ItemStack(Material.ARROW));
			}

			if (Bukkit.getPlayer(damager).getInventory().contains(Material.valueOf(plugin.getItems().getConfig().getString("build-block.material").toUpperCase()))){
				Bukkit.getPlayer(damager).getInventory().addItem(buildBlockItem.createBuildBlockItem(4));
			} else {
				Bukkit.getPlayer(damager).getInventory().setItem(8, buildBlockItem.createBuildBlockItem(4));
			}
			damagerMap.removeDamager(player.getUniqueId());
			// Teleportace hráče na spawn
			arenaManager.teleportPlayerToCurrentArena(player);

			player.getInventory().remove(new ItemStack(Material.ENDER_PEARL));
			player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
			player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
			player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

			Bukkit.getPlayer(damager).playSound(Bukkit.getPlayer(damager).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
		}
	}

}

