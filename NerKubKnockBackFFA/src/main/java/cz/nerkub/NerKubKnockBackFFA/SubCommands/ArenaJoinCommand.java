package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaJoinCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private ArenaManager arenaManager;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final BuildBlockItem buildBlockItem;
	private final ScoreBoardManager scoreBoardManager;
	private final RankManager rankManager;
	private InventoryManager inventoryManager;

	private final KillsMap killsMap;

	public ArenaJoinCommand(NerKubKnockBackFFA plugin, ArenaManager arenaManager, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem, ScoreBoardManager scoreBoardManager, RankManager rankManager, InventoryManager inventoryManager, KillsMap killsMap) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
		this.scoreBoardManager = scoreBoardManager;
		this.rankManager = rankManager;
		this.inventoryManager = inventoryManager;
		this.killsMap = killsMap;
	}

	@Override
	public String getName() {
		return "join";
	}

	@Override
	public String getDescription() {
		return "&7Joins the player to the current arena.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa join";
	}

	@Override
	public boolean perform(Player player, String[] args) {

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("bungee-true-join")));
			return true;
		}

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix")
				+ plugin.getMessages().getConfig().getString("arena-join")));

		String currentArena = plugin.getArenaManager().getCurrentArena();

		inventoryManager.saveInventory(player);
		inventoryManager.saveLocation(player);

		player.getInventory().clear();



		if (currentArena != null) {
			// Přiřazení hráče do arény
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

			// Získat kills z killsMap
			Integer kills = killsMap.getInt(player.getUniqueId());

			// Zkontrolovat, zda je kills null nebo 0
			if (kills == null || kills == 0) {
				kills = 0; // Pokud je null nebo 0, nastav na 0
			}

			rankManager.savePlayerRank(player);
			arenaManager.teleportPlayerToCurrentArena(player);

			scoreBoardManager.startScoreboardUpdater(player);
			scoreBoardManager.updateScoreboard(player);

		}

		player.setGameMode(GameMode.SURVIVAL);

		return false;
	}
}
