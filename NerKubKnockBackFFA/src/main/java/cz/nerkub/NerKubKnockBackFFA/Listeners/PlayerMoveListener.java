package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.DeathsMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
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
	private final DatabaseManager databaseManager;
	private final DamagerMap damagerMap;
	private final KillStreakMap killStreakMap;
	private final DeathsMap deathsMap;
	private final Random random;
	private final BuildBlockItem buildBlockItem;
	private final ArenaManager arenaManager;
	private final RankManager rankManager;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final MaxItemInInvListener maxItemInInvListener;
	private final DefaultInventoryManager defaultInventoryManager;


	public PlayerMoveListener(NerKubKnockBackFFA plugin, Random random, DatabaseManager databaseManager, DamagerMap damagerMap, KillStreakMap killStreakMap, DeathsMap deathsMap, BuildBlockItem buildBlockItem, ArenaManager arenaManager, RankManager rankManager, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, MaxItemInInvListener maxItemInInvListener, DefaultInventoryManager defaultInventoryManager) {
		this.plugin = plugin;
		this.databaseManager = databaseManager;
		this.damagerMap = damagerMap;
		this.killStreakMap = killStreakMap;
		this.deathsMap = deathsMap;
		this.buildBlockItem = buildBlockItem;
		this.arenaManager = arenaManager;
		this.rankManager = rankManager;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.maxItemInInvListener = maxItemInInvListener;
		this.defaultInventoryManager = defaultInventoryManager;
		this.random = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerUUID = player.getUniqueId();
		String prefix = plugin.getMessages().getConfig().getString("prefix");

		int deathHeight = plugin.getConfig().getInt("death-height");

		// Pokud hráč spadne pod určitou výšku (smrt)
		if (event.getFrom().getY() > deathHeight && event.getTo().getY() <= deathHeight) {

			// 📌 Získání statistik hráče
			PlayerStats stats = plugin.getPlayerStatsManager().getStats(playerUUID);
			if (stats == null) {
				return;
			}

			if (!damagerMap.hasDamager(playerUUID)) {
				handleDeathWithoutDamager(player, stats);
				return;
			}

			UUID DamagerUUID = damagerMap.getDamager(playerUUID);
			Player damager = Bukkit.getPlayer(DamagerUUID);
			if (damager == null) {
				return;
			}

			handleDeathByPlayer(player, damager, stats);


		}
	}

	// 📌 Pomocná metoda pro reset hráče po smrti
	private void resetPlayer(Player player) {
		arenaManager.joinCurrentArena(player);
		player.getInventory().clear();

		defaultInventoryManager.setPlayerInventory(player);

		player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
	}

	private void handleDeathWithoutDamager(Player player, PlayerStats stats) {
		UUID playerUUID = player.getUniqueId();
		killStreakMap.resetKillStreak(playerUUID);
		deathsMap.putInt(playerUUID);
		stats.setDeaths(stats.getDeaths() + 1);

		int minDeath = plugin.getConfig().getInt("elo.death-min");
		int maxDeath = plugin.getConfig().getInt("elo.death-max");
		int eloLost = random.nextInt(maxDeath - minDeath + 1) + minDeath;
		stats.setElo(stats.getElo() - eloLost);

		plugin.getPlayerStatsManager().saveStats(playerUUID);

		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				plugin.getMessages().getConfig().getString("prefix") +
						plugin.getMessages().getConfig().getString("elo.elo-lost")
								.replace("%elo%", Integer.toString(eloLost))));

		rankManager.savePlayerRank(player);
		resetPlayer(player);
		damagerMap.removeDamager(playerUUID);

		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "LowGravity")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "LowGravity");
		}
		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "ExtraPunchBow")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "ExtraPunchBow");
		}
		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "NoKnockBackStick")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "NoKnockBackStick");
		}
	}

	private void handleDeathByPlayer(Player player, Player damager, PlayerStats stats) {
		UUID playerUUID = player.getUniqueId();
		UUID damagerUUID = damager.getUniqueId();

		PlayerStats damagerStats = plugin.getPlayerStatsManager().getStats(damagerUUID);
		if (damagerStats == null) {
			return;
		}

		int eloGain = random.nextInt(plugin.getConfig().getInt("elo.kill-max") - plugin.getConfig().getInt("elo.kill-min") + 1) + plugin.getConfig().getInt("elo.kill-min");
		int eloLoss = random.nextInt(plugin.getConfig().getInt("elo.death-max") - plugin.getConfig().getInt("elo.death-min") + 1) + plugin.getConfig().getInt("elo.death-min");

		damagerStats.setKills(damagerStats.getKills() + 1);
		stats.setDeaths(stats.getDeaths() + 1);
		damagerStats.setElo(damagerStats.getElo() + eloGain);
		stats.setElo(stats.getElo() - eloLoss);

		rankManager.savePlayerRank(damager);
		rankManager.savePlayerRank(player);

		Set<String> keys = plugin.getMessages().getConfig().getConfigurationSection("kill-messages").getKeys(false);
		List<String> keyList = new ArrayList<>(keys);
		String randomKey = keyList.get(random.nextInt(keyList.size()));

		int coinMinKill = plugin.getConfig().getInt("coins.kill-min");
		int coinMaxKill = plugin.getConfig().getInt("coins.kill-max");
		int coinGained = random.nextInt(coinMaxKill - coinMinKill + 1) + coinMinKill;
		damagerStats.setCoins(damagerStats.getCoins() + coinGained);

		// 🔫 Zvýšení killstreaku pro damagera
		int currentKillStreak = killStreakMap.putInt(damagerUUID); // Zvýšení o 1

		// 📈 Aktualizace maximálního killstreaku
		if (currentKillStreak > damagerStats.getMaxKillstreak()) {
			damagerStats.setMaxKillstreak(currentKillStreak);
			plugin.getPlayerStatsManager().saveStats(damagerUUID);
		}

		// 🔹 Odměna za každých 5 killů
		if (currentKillStreak % 5 == 0 && currentKillStreak > 0) {
			int reward = currentKillStreak * plugin.getConfig().getInt("coins.kill-streak", 5);
			damagerStats.setCoins(damagerStats.getCoins() + reward);

			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("kill-streak")
							.replace("%player%", damager.getDisplayName())
							.replace("%killstreak%", String.valueOf(currentKillStreak))
			));

			damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("coins.coins-kill-streak-gained")
							.replace("%coins%", String.valueOf(reward))
							.replace("%killstreak%", String.valueOf(currentKillStreak))
			));
		}
		killStreakMap.resetKillStreak(playerUUID);
		plugin.getPlayerStatsManager().saveStats(damagerUUID);
		plugin.getPlayerStatsManager().saveStats(playerUUID);

		Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
				plugin.getMessages().getConfig().getString("prefix") +
						plugin.getMessages().getConfig().getString("kill-messages." + randomKey)
								.replace("%player1%", damager.getDisplayName())
								.replace("%player2%", player.getDisplayName())));

		plugin.getPlayerStatsManager().saveStats(damagerUUID);
		plugin.getPlayerStatsManager().saveStats(playerUUID);

		maxItemInInvListener.checkPlayerInventory(damager);

		damager.playSound(damager.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		damager.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
		damager.getInventory().addItem(new ItemStack(Material.ARROW));
		damager.getInventory().addItem(buildBlockItem.createBuildBlockItem(4));

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("elo.elo-lost")
				.replace("%elo%", String.valueOf(eloLoss))));
		damager.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("coins.coins-gained")
				.replace("%coins%", String.valueOf(coinGained))));
		damager.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("elo.elo-gained")
				.replace("%elo%", String.valueOf(eloGain))));

		resetPlayer(player);
		damagerMap.removeDamager(playerUUID);

		// ✅ Odstraníme hráče z aktivních eventů, protože byl eliminován
		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "LowGravity")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "LowGravity");
		}
		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "ExtraPunchBow")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "ExtraPunchBow");
		}
		if (plugin.getCustomEventManager().hasPlayerEvent(playerUUID, "NoKnockBackStick")) {
			plugin.getCustomEventManager().removePlayerFromEvent(playerUUID, "NoKnockBackStick");
		}
	}


}


