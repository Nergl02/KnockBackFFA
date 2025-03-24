package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class ClearArrowsSubcommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public ClearArrowsSubcommand(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}

	@Override
	public String getName() {
		return "cleararrows";
	}

	@Override
	public String getDescription() {
		return "&7Clears all arrows inside arena";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa cleararrows &e<arenaName>";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (args.length != 2 || !args[0].equalsIgnoreCase("cleararrows")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("usage.remove-arena").replace("%arena%", args[1])));
			return true;
		}
		String targetArena = args[1];

		Location min = arenaManager.getArenaMinBounds(targetArena);
		Location max = arenaManager.getArenaMaxBounds(targetArena);

		if (min == null || max == null) {
			if (plugin.getConfig().getBoolean("debug")) {
				Bukkit.getLogger().info("⚠️ Arena '" + targetArena + "' was not found.");
			}
			return true;
		}

		World world = min.getWorld();
		int count = 0;

		for (Entity entity : world.getEntitiesByClass(Arrow.class)) {
			Location loc = entity.getLocation();
			if (isInBounds(loc, min, max)) {
				entity.remove();
				count++;
			}
		}

		if (plugin.getConfig().getBoolean("debug")) {
			Bukkit.getLogger().info("✅ Removed " + count + " arrows in arena '" + targetArena + "'.");
		}
		return true;
	}

	private boolean isInBounds(Location loc, Location min, Location max) {
		return loc.getWorld().equals(min.getWorld())
				&& loc.getX() >= min.getX() && loc.getX() <= max.getX()
				&& loc.getY() >= min.getY() && loc.getY() <= max.getY()
				&& loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
	}
}
