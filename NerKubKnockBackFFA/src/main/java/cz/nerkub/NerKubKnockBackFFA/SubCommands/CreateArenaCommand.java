package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.PrimitiveIterator;

public class CreateArenaCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public CreateArenaCommand(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}

	@Override
	public String getName() {
		return "createarena";
	}

	@Override
	public String getDescription() {
		return "&7Create an arena";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa createarena &e<arenaName>";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("no-permission")));
			return true;
		}

		if (args.length != 2 || !args[0].equalsIgnoreCase("createarena")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("usage.create-arena").replace("%arena%", args[2])));
			return true;
		}

		String arenaName = args[1];

		if (arenaManager.doesArenaExist(arenaName)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.already-exists").replace("%arena%", arenaName)));
			return true;
		}

		// Vytvoření arény
		arenaManager.createArena(player, arenaName);

		return true;
	}
}
