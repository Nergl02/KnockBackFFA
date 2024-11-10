package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.DeathsMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.RankManager;
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
	private final KillStreakMap killStreakMap;
	private final KillsMap killsMap;
	private final DeathsMap deathsMap;
	private final Random random;
	private final BuildBlockItem buildBlockItem;
	private final ArenaManager arenaManager;
	private final RankManager rankManager;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;


	public PlayerMoveListener(NerKubKnockBackFFA plugin, Random random, DamagerMap damagerMap, KillStreakMap killStreakMap, KillsMap killsMap, DeathsMap deathsMap, BuildBlockItem buildBlockItem, ArenaManager arenaManager, RankManager rankManager, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
		this.killStreakMap = killStreakMap;
		this.killsMap = killsMap;
		this.deathsMap = deathsMap;
		this.buildBlockItem = buildBlockItem;
		this.arenaManager = arenaManager;
		this.rankManager = rankManager;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		Player player = event.getPlayer();

		if (player.getLocation().getY() <= plugin.getConfig().getInt("death-height")) {
			// POKUD NENÍ DAMAGER A HRÁČ SPADNE SÁM
			if (!damagerMap.hasDamager(player.getUniqueId())) {

				arenaManager.teleportPlayerToCurrentArena(player);
				killStreakMap.removeInt(player.getUniqueId());
				deathsMap.putInt(player.getUniqueId());

				player.getInventory().clear();

				player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
				player.getInventory().setItem(2, punchBowItem.createBowItem());
				player.getInventory().setItem(0, knockBackStickItem.createKnockBackStickItem());
				player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
				player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
				player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

				player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);

				// Uložení deaths
				int deaths = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".deaths", 0);
				deaths++;
				plugin.getPlayers().getConfig().set(player.getDisplayName() + ".deaths", deaths);

				plugin.getPlayers().saveConfig();

				int minDeath = plugin.getConfig().getInt("elo.death-min");
				int maxDeath = plugin.getConfig().getInt("elo.death-max");

				if (minDeath > maxDeath) {
					Bukkit.getLogger().severe("Error: death-max must be greater than or equal to death-min!");
					return;
				}

				int eloLost = random.nextInt(maxDeath - minDeath + 1) + minDeath;
				int currentElo = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".elo");
				int newElo = currentElo - eloLost;

				Bukkit.getLogger().info("Current ELO: " + currentElo);
				Bukkit.getLogger().info("ELO lost: " + eloLost);
				Bukkit.getLogger().info("New ELO: " + newElo);

				plugin.getPlayers().getConfig().set(player.getDisplayName() + ".elo", newElo);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("elo.elo-lost").replace("%elo%", Integer.toString(eloLost))));
				rankManager.savePlayerRank(player);
				plugin.getPlayers().saveConfig();


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

			if (Bukkit.getPlayer(damager).getInventory().contains(Material.valueOf(plugin.getItems().getConfig().getString("build-block.material").toUpperCase()))) {
				Bukkit.getPlayer(damager).getInventory().addItem(buildBlockItem.createBuildBlockItem(4));
			} else {
				Bukkit.getPlayer(damager).getInventory().setItem(8, buildBlockItem.createBuildBlockItem(4));
			}

			// Uložení zabití do databáze

			damagerMap.removeDamager(player.getUniqueId());
			killStreakMap.putInt(damager);
			killStreakMap.removeInt(player.getUniqueId());
			deathsMap.putInt(player.getUniqueId());

			// Uložení killu
			int kills = plugin.getPlayers().getConfig().getInt(Bukkit.getPlayer(damager).getDisplayName() + ".kills", 0);
			kills++;
			plugin.getPlayers().getConfig().set(Bukkit.getPlayer(damager).getDisplayName() + ".kills", kills);

			// Uložení deaths
			int deaths = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".deaths", 0);
			deaths++;
			plugin.getPlayers().getConfig().set(player.getDisplayName() + ".deaths", deaths);

			plugin.getPlayers().saveConfig();

			killsMap.putInt(damager);

			int eloMinDeath = plugin.getConfig().getInt("elo.death-min");
			int eloMaxDeath = plugin.getConfig().getInt("elo.death-max");

			// Kontrola platnosti minimální a maximální hodnoty pro smrt
			if (eloMinDeath > eloMaxDeath) {
				Bukkit.getLogger().severe("Error: death-max must be greater than or equal to death-min!");
				return; // nebo jiná logika pro zpracování chyby
			}

			int eloLost = random.nextInt(eloMaxDeath - eloMinDeath + 1) + eloMinDeath;
			int currentElo = plugin.getPlayers().getConfig().getInt(player.getDisplayName() + ".elo", 0);
			int newElo = currentElo - eloLost;

			plugin.getPlayers().getConfig().set(player.getDisplayName() + ".elo", newElo);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("elo.elo-lost").replace("%elo%", Integer.toString(eloLost))));

			int minKill = plugin.getConfig().getInt("elo.kill-min");
			int maxKill = plugin.getConfig().getInt("elo.kill-max");

			// Kontrola platnosti minimální a maximální hodnoty pro zabití
			if (minKill > maxKill) {
				Bukkit.getLogger().severe("Error: kill-max must be greater than or equal to kill-min!");
				return; // nebo jiná logika pro zpracování chyby
			}

			int eloGained = random.nextInt(maxKill - minKill + 1) + minKill;
			int currentEloDamager = plugin.getPlayers().getConfig().getInt(Bukkit.getPlayer(damager).getDisplayName() + ".elo", 0);
			int newEloDamager = currentEloDamager + eloGained;

			int coinMinKill = plugin.getConfig().getInt("coins.kill-min");
			int coinMaxKill = plugin.getConfig().getInt("coins.kill-max");
			int currentCoin = plugin.getPlayers().getConfig().getInt(Bukkit.getPlayer(damager).getDisplayName() + ".coins", 0);
			int coinGained = random.nextInt(coinMaxKill - coinMinKill + 1) + coinMinKill;
			int newCoin = currentCoin + coinGained;

			plugin.getPlayers().getConfig().set(Bukkit.getPlayer(damager).getDisplayName() + ".elo", newEloDamager);
			plugin.getPlayers().getConfig().set(Bukkit.getPlayer(damager).getDisplayName() + ".coins", newCoin);
			Bukkit.getPlayer(damager).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("elo.elo-gained").replace("%elo%", Integer.toString(eloGained))));
			Bukkit.getPlayer(damager).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("coins.coins-gained").replace("%coins%", Integer.toString(coinGained))));

			// Nezapomeň uložit změny
			plugin.getPlayers().saveConfig();
			rankManager.savePlayerRank(Bukkit.getPlayer(damager));
			rankManager.savePlayerRank(player);

			// Získání hodnoty kill streaku
			Integer killStreak = killStreakMap.getInt(damager);
			if (killStreak != null && killStreak % 5 == 0 && killStreak > 0) {

				int killStreakReward = plugin.getConfig().getInt("coins.kill-streak");
				int killStreakGained = killStreak * killStreakReward;

				newCoin = currentCoin + killStreakGained + coinGained;
				plugin.getPlayers().getConfig().set(Bukkit.getPlayer(damager).getDisplayName() + ".coins", newCoin);

				Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("kill-streak")
								.replace("%player%", Bukkit.getPlayer(damager).getDisplayName())
								.replace("%killstreak%", String.valueOf(killStreak))
				));

				Bukkit.getPlayer(damager).sendMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("coins.coins-kill-streak-gained")
								.replace("%coins%", String.valueOf(killStreakGained))
								.replace("%killstreak%", String.valueOf(killStreak))
				));

			}

			int currentKillStreak = plugin.getPlayers().getConfig().getInt(Bukkit.getPlayer(damager).getDisplayName() + ".max-kill-streak");

			if (killStreakMap.getInt(damager) > currentKillStreak) {
				plugin.getPlayers().getConfig().set(Bukkit.getPlayer(damager).getDisplayName() + ".max-kill-streak", killStreakMap.getInt(damager));
				plugin.getPlayers().saveConfig();
			}

			// Teleportace hráče na spawn
			arenaManager.teleportPlayerToCurrentArena(player);

			player.getInventory().clear();

			player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
			player.getInventory().setItem(2, punchBowItem.createBowItem());
			player.getInventory().setItem(0, knockBackStickItem.createKnockBackStickItem());

			player.getInventory().remove(new ItemStack(Material.ENDER_PEARL));
			player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
			player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
			player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

			Bukkit.getPlayer(damager).playSound(Bukkit.getPlayer(damager).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
		}
	}

}

