package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetArenaSpawnCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;

	public SetArenaSpawnCommand(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "setspawn";
	}

	@Override
	public String getDescription() {
		return "&7Set spawn for specific arena";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa setspawn &e<arenaName>";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		Location spawn = player.getLocation();
		String arenaName = args[1].toString();

		plugin.getArenas().getConfig().set(arenaName + ".spawn.world", spawn.getWorld().getName());
		plugin.getArenas().getConfig().set(arenaName + ".spawn.x", spawn.getX());
		plugin.getArenas().getConfig().set(arenaName + ".spawn.y", spawn.getY());
		plugin.getArenas().getConfig().set(arenaName + ".spawn.z", spawn.getZ());
		plugin.getArenas().getConfig().set(arenaName + ".spawn.yaw", spawn.getYaw());
		plugin.getArenas().getConfig().set(arenaName + ".spawn.pitch", spawn.getPitch());

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("arena-created").replace("%arena%", args[1])));
		plugin.getArenas().saveConfig();

		return false;
	}
}
