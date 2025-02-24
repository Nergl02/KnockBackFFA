package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaToolCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public ArenaToolCommand(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}

	@Override
	public String getName() {
		return "tool";
	}

	@Override
	public String getDescription() {
		return "&7Gives you an arena setup tool.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa arenatool";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("no-permission")));
			return true;
		}

		arenaManager.giveTool(player);
		return true;
	}
}
