package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaListCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;

	public ArenaListCommand(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "arenalist";
	}

	@Override
	public String getDescription() {
		return "&7Return list of &aall arenas&7.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa arenalist";
	}

	@Override
	public boolean perform(Player player, String[] args) {

		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		if (plugin.getArenas().getConfig().getKeys(false).isEmpty()) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") + plugin.getMessages().getConfig().getString("no-arenas")));
		}

		for (String key : plugin.getArenas().getConfig().getKeys(false)) {
			player.sendMessage(key);
		}

		return false;
	}
}
