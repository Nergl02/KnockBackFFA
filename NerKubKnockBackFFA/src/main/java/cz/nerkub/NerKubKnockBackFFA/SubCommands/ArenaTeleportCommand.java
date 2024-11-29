package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaTeleportCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;

	public ArenaTeleportCommand(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "arenatp";
	}

	@Override
	public String getDescription() {
		return "&7Teleport you to a specific arena.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa arenatp &e<arenaName>";
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
							plugin.getMessages().getConfig().getString("usages.teleport-arena")));
			return false; // Příkaz selhal, protože nebyl zadán správný název arény
		}

		String arenaName = args[1];

		if (plugin.getArenas().getConfig().contains(arenaName)) {
			String worldName = plugin.getArenas().getConfig().getString(arenaName + ".spawn.world");
			Double x = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.x");
			Double y = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.y");
			Double z = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.z");
			Float yaw = (float) plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.yaw");
			Float pitch = (float) plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.pitch");

			Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

			player.teleport(location);


			// Výstup pro potvrzení odstranění
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix")
					+ plugin.getMessages().getConfig().getString("tp-arena").replace("%arena%", arenaName)));
			return true;
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix")
					+ plugin.getMessages().getConfig().getString("invalid-arena").replace("%arena%", arenaName)));
			return false;
		}

	}
}
