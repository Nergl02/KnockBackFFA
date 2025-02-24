package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveArenaCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public RemoveArenaCommand(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}


	@Override
	public String getName() {
		return "removearena";
	}

	@Override
	public String getDescription() {
		return "&7Remove a specific arena";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa removearena &e<arenaName>";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");

		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("no-permission")));
			return true;
		}

		if (args.length != 2 || !args[0].equalsIgnoreCase("removearena")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("usage.remove-arena").replace("%arena%", args[1])));
			return true;
		}

		String arenaName = args[1];

		arenaManager.removeArena(player, arenaName);

		return true;

	}
}
