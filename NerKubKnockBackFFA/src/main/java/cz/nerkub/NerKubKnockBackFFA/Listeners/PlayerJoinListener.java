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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;


public class PlayerJoinListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final BuildBlockItem buildBlockItem;
	private final ArenaManager arenaManager;
	private final ScoreBoardManager scoreBoardManager;
	private final DatabaseManager databaseManager;
	private final DefaultInventoryManager defaultInventoryManager;

	private final DamagerMap damagerMap;
	private final KillStreakMap killStreakMap;
	private final KillsMap killsMap;
	private final RankManager rankManager;
	private InventoryRestoreManager inventoryRestoreManager;

	public PlayerJoinListener(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DatabaseManager databaseManager, DefaultInventoryManager defaultInventoryManager, DamagerMap damagerMap, KillStreakMap killStreakMap, KillsMap killsMap, RankManager rankManager, InventoryRestoreManager inventoryRestoreManager) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
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

		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			// Pokud je bungee-mode vypnutý, obnovíme inventář a pozici
			inventoryRestoreManager.restoreInventory(player);
			inventoryRestoreManager.restoreLocation(player);
			scoreBoardManager.removeScoreboard(player);
		}

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			if (currentArena != null) {
				// Přiřazení hráče do arény
				plugin.getScoreBoardManager().removeScoreboard(player);
				plugin.getScoreBoardManager().startScoreboardUpdater(player);
				plugin.getScoreBoardManager().updateScoreboard(player);

				player.getInventory().clear();
				defaultInventoryManager.setPlayerInventory(player);

				// TODO
				// if in config.yml join-message set to true, take join-message from messages.yml if false, set to null
				event.setJoinMessage(null);

				// Získat kills z killsMap
				Integer kills = killsMap.getInt(player.getUniqueId());

				// Zkontrolovat, zda je kills null nebo 0
				if (kills == null || kills == 0) {
					kills = 0; // Pokud je null nebo 0, nastav na 0
				}

				rankManager.savePlayerRank(player);

				if (!currentArena.equals("Žádná aréna")) {
					arenaManager.joinCurrentArena(player);
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

		event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("leave-message").replace("%player%", player.getName().toString())));

		plugin.getBossBarManager().removePlayer(player);
	}
}
