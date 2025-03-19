package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class PlayerJoinListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;
	private final ScoreBoardManager scoreBoardManager;
	private final DatabaseManager databaseManager;
	private final DefaultInventoryManager defaultInventoryManager;

	private final DamagerMap damagerMap;
	private final KillStreakMap killStreakMap;
	private final KillsMap killsMap;
	private final RankManager rankManager;
	private InventoryRestoreManager inventoryRestoreManager;

	public PlayerJoinListener(NerKubKnockBackFFA plugin, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DatabaseManager databaseManager, DefaultInventoryManager defaultInventoryManager, DamagerMap damagerMap, KillStreakMap killStreakMap, KillsMap killsMap, RankManager rankManager, InventoryRestoreManager inventoryRestoreManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.scoreBoardManager = scoreBoardManager;
		this.databaseManager = databaseManager;
		this.defaultInventoryManager = defaultInventoryManager;
		this.damagerMap = damagerMap;
		this.killStreakMap = killStreakMap;
		this.killsMap = killsMap;
		this.rankManager = rankManager;
		this.inventoryRestoreManager = inventoryRestoreManager;

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		Player player = event.getPlayer();
		String currentArena = arenaManager.getCurrentArenaName();

		databaseManager.insertPlayer(String.valueOf(player.getUniqueId()), player.getName());

		// Pokud hráč hraje poprvé, teleportujeme ho do arény
		if (!player.hasPlayedBefore()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (currentArena != null && !currentArena.equals("Žádná aréna")) {
					Location arenaSpawn = plugin.getArenaManager().getArenaSpawn(currentArena);
					if (arenaSpawn != null) {
						player.teleport(arenaSpawn);
						Bukkit.getLogger().info("[DEBUG] Player " + player.getName() + " joined the server.");
						if (!player.hasPlayedBefore()) {
							Bukkit.getLogger().info("[DEBUG] First time join detected!");
						}
						Bukkit.getLogger().info("[DEBUG] Current arena: " + currentArena);

					}
				}
			}, 20L); // 1 sekunda delay
		}

		// Pokud není bungee-mode, obnovíme inventář a pozici
		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			inventoryRestoreManager.restoreInventory(player);
			inventoryRestoreManager.restoreLocation(player);
		}

		plugin.getScoreBoardManager().removeScoreboard(player);
		plugin.getScoreBoardManager().startScoreboardUpdater(player);
		plugin.getScoreBoardManager().updateScoreboard(player);

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			if (currentArena != null) {
				player.getInventory().clear();
				defaultInventoryManager.setPlayerInventory(player);

				event.setJoinMessage(null);

				Integer kills = killsMap.getInt(player.getUniqueId());
				if (kills == null) kills = 0;

				rankManager.savePlayerRank(player);

				if (!currentArena.equals("Žádná aréna")) {
					arenaManager.joinCurrentArena(player);
				}

				// Pokud je aktivní LowGravity, přidej efekty
				if (plugin.getCustomEventManager().isEventActive("LowGravity")) {
					player.removePotionEffect(PotionEffectType.JUMP);
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -2)); // LowGravity efekt
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0)); // Mírnější dopad
				}

				// Pokud je aktivní ExtraPunchBow, dej hráči luk
				if (plugin.getCustomEventManager().isEventActive("ExtraPunchBow")) {
					ItemStack bow = new ItemStack(Material.BOW);
					ItemMeta meta = bow.getItemMeta();
					if (meta != null) {
						meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 5, true);
						bow.setItemMeta(meta);
					}
					player.getInventory().setItem(2, bow);
				}
			}
		}

		player.setGameMode(GameMode.SURVIVAL);
		event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("join-message").replace("%player%", player.getName().toString())));

		plugin.getBossBarManager().updateBossBar();
	}


	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		Player player = event.getPlayer();

		// Odstraní kill streak a damagera
		killStreakMap.resetKillStreak(player.getUniqueId());
		damagerMap.removeDamager(player.getUniqueId());

		// Nastaví quit message na null (nebudete mít žádnou zprávu při odchodu)
		event.setQuitMessage(null);

		// Uložení hráčů a skóre
		scoreBoardManager.stopScoreboardUpdater(player);
		scoreBoardManager.removeScoreboard(player);

		// Obnovení inventáře a pozice, pokud není bungee-mode
		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			inventoryRestoreManager.restoreInventory(player);
			inventoryRestoreManager.restoreLocation(player);
		}

		if (plugin.getArenaManager().isPlayerInArena(player)) {
			plugin.getDatabaseManager().removePlayerFromArena(player.getUniqueId());
			plugin.getArenaManager().getPlayersInArena().remove(player.getUniqueId());
		}

		if (plugin.getCustomEventManager().isEventActive("LowGravity")) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.removePotionEffect(PotionEffectType.SLOW_FALLING);
		}

		event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("leave-message").replace("%player%", player.getName().toString())));

		plugin.getBossBarManager().removePlayer(player);
	}
}
