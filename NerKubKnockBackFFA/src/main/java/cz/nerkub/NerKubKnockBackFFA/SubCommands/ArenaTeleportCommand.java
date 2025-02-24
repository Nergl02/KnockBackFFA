package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		if (args.length < 2) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("usages.teleport-arena")));
			return false;
		}

		String arenaName = args[1];

		// Načtení informací z databáze
		String sql = "SELECT world, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch FROM arenas WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {
					String worldName = rs.getString("world");
					double x = rs.getDouble("spawn_x");
					double y = rs.getDouble("spawn_y");
					double z = rs.getDouble("spawn_z");
					float yaw = rs.getFloat("spawn_yaw");
					float pitch = rs.getFloat("spawn_pitch");

					World world = Bukkit.getWorld(worldName);
					if (world == null) {
						player.sendMessage(ChatColor.RED + "❌ Svět '" + worldName + "' pro arénu '" + arenaName + "' nebyl nalezen.");
						return false;
					}

					Location location = new Location(world, x, y, z, yaw, pitch);
					player.teleport(location);

					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getMessages().getConfig().getString("prefix") +
									plugin.getMessages().getConfig().getString("tp-arena").replace("%arena%", arenaName)));
					return true;
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getMessages().getConfig().getString("prefix") +
									plugin.getMessages().getConfig().getString("invalid-arena").replace("%arena%", arenaName)));
					return false;
				}
			}

		} catch (SQLException e) {
			player.sendMessage(ChatColor.RED + "❌ Chyba při načítání informací o aréně z databáze.");
			e.printStackTrace();
			return false;
		}
	}
}
