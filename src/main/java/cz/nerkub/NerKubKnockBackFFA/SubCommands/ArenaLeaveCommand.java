package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;


public class ArenaLeaveCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private InventoryManager inventoryManager;
	private final ArenaManager arenaManager;
	private final ScoreBoardManager scoreBoardManager;
	private final DamagerMap damagerMap;

	public ArenaLeaveCommand(NerKubKnockBackFFA plugin, InventoryManager inventoryManager, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.inventoryManager = inventoryManager;
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
		inventoryManager.restoreInventory(player);
		inventoryManager.restoreLocation(player);

		// TODO nefunguje odstranění scoreboardu
		// Odstraň skóreboard
		scoreBoardManager.removeScoreboard(player);

		return false;
	}
}
