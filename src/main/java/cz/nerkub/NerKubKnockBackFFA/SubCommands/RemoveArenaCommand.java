package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveArenaCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;

	public RemoveArenaCommand(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
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

		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		// Zkontrolujte, zda je argument pro název arény platný
		if (args.length < 2) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("usages.remove-arena")));
			return false; // Příkaz selhal, protože nebyl zadán správný název arény
		}

		String arenaName = args[1];

		if (plugin.getArenas().getConfig().contains(arenaName)) {
			// Nastavte hodnotu na null, čímž arénu odstraníte z configu
			plugin.getArenas().getConfig().set(arenaName, null);

			// Uložte změny do souboru arenas.yml
			plugin.getArenas().saveConfig();

			// Výstup pro potvrzení odstranění
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix")
					+ plugin.getMessages().getConfig().getString("arena-removed").replace("%arena%", arenaName)));
			return true;
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix")
					+ plugin.getMessages().getConfig().getString("invalid-arena").replace("%arena%", arenaName)));
			return false;
		}

	}
}
