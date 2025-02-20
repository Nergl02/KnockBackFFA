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
import org.bukkit.inventory.ItemFactory;
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

	private final DamagerMap damagerMap;
	private final KillStreakMap killStreakMap;
	private final KillsMap killsMap;
	private final RankManager rankManager;
	private InventoryManager inventoryManager;

	public PlayerJoinListener(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DatabaseManager databaseManager, DamagerMap damagerMap, KillStreakMap killStreakMap, KillsMap killsMap, RankManager rankManager, InventoryManager inventoryManager) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
		this.arenaManager = arenaManager;
		this.scoreBoardManager = scoreBoardManager;
		this.databaseManager = databaseManager;
		this.damagerMap = damagerMap;
		this.killStreakMap = killStreakMap;
		this.killsMap = killsMap;
		this.rankManager = rankManager;
		this.inventoryManager = inventoryManager;

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String currentArena = plugin.getArenaManager().getCurrentArena();

		databaseManager.insertPlayer(String.valueOf(player.getUniqueId()), player.getName());

		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			// Pokud je bungee-mode vypnutý, obnovíme inventář a pozici
			inventoryManager.restoreInventory(player);
			inventoryManager.restoreLocation(player);
			scoreBoardManager.removeScoreboard(player);
		}

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			if (currentArena != null) {
				// Přiřazení hráče do arény
				plugin.getScoreBoardManager().removeScoreboard(player);
				plugin.getScoreBoardManager().startScoreboardUpdater(player);
				plugin.getScoreBoardManager().updateScoreboard(player);

				player.getInventory().clear();
				player.getInventory().setItem(0, knockBackStickItem.createKnockBackStickItem());
				player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
				player.getInventory().setItem(2, punchBowItem.createBowItem());
				player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
				player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
				player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

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

				arenaManager.teleportPlayerToCurrentArena(player);

			}

		}
		player.setGameMode(GameMode.SURVIVAL);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		// Odstraní kill streak a damagera
		killStreakMap.removeInt(player.getUniqueId());
		damagerMap.removeDamager(player.getUniqueId());

		// Nastaví quit message na null (nebudete mít žádnou zprávu při odchodu)
		event.setQuitMessage(null);

		// Uložení hráčů a skóre
		scoreBoardManager.removeScoreboard(player);

		// Obnovení inventáře a pozice, pokud není bungee-mode
		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			inventoryManager.restoreInventory(player);
			inventoryManager.restoreLocation(player);
		}
	}
}
