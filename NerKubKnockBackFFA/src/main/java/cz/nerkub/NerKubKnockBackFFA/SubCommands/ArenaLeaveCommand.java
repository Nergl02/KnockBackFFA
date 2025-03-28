package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class ArenaLeaveCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private InventoryRestoreManager inventoryRestoreManager;
	private final ArenaManager arenaManager;
	private final ScoreBoardManager scoreBoardManager;
	private final DamagerMap damagerMap;

	public ArenaLeaveCommand(NerKubKnockBackFFA plugin, InventoryRestoreManager inventoryRestoreManager, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.inventoryRestoreManager = inventoryRestoreManager;
		this.arenaManager = arenaManager;
		this.scoreBoardManager = scoreBoardManager;
		this.damagerMap = damagerMap;
	}

	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public String getDescription() {
		return "&7Remove the player to the current arena.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa leave";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		// Zkontroluj, zda hráč není v aréně
		if (!arenaManager.isPlayerInArena(player)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("player-leave-command-not-in-arena")));
			return true;  // Pokud hráč není v aréně, ukončím metodu
		}

		// Pokud je Bungee mód aktivní, pošli zprávu
		if (plugin.getConfig().getBoolean("bungee-mode")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("bungee-true-join")));
			return true;
		}

		// Pošli zprávu, že hráč opustil arénu
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
				plugin.getMessages().getConfig().getString("arena-leave")));

		// Obnov inventář a lokaci hráče
		inventoryRestoreManager.restoreInventory(player);
		inventoryRestoreManager.restoreLocation(player);

		// TODO nefunguje odstranění scoreboardu
		// Odstraň skóreboard
		scoreBoardManager.removeScoreboard(player);

		return false;
	}
}
