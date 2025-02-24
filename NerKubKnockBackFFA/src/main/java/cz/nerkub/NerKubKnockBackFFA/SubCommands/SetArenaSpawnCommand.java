package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetArenaSpawnCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;

	public SetArenaSpawnCommand(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "setarenaspawn";
	}

	@Override
	public String getDescription() {
		return "&7Set spawn for specific arena";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa setarenaspawn &e<arenaName>";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");

		// Ověření oprávnění
		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		// Kontrola argumentů
		if (args.length < 2) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("usages.setspawn-arena")));
			return false;
		}

		String arenaName = args[1];

		// Ověření existence arény
		if (!plugin.getArenaManager().doesArenaExist(arenaName)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.invalid-arena").replace("%arena%", arenaName)));
			return false;
		}

		// Nastavení spawnu pomocí ArenaManageru
		plugin.getArenaManager().setArenaSpawn(player, arenaName, player.getLocation());

		return true;
	}

}
