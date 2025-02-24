package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		String sql = "SELECT arena_name, is_active FROM arenas;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			if (!rs.isBeforeFirst()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getMessages().getConfig().getString("prefix") +
								plugin.getMessages().getConfig().getString("no-arenas")));
				return false;
			}

			player.sendMessage(ChatColor.GOLD + "List of available arenas::");
			while (rs.next()) {
				String arenaName = rs.getString("arena_name");
				boolean isActive = rs.getBoolean("is_active");

				String status = isActive ? ChatColor.GREEN + " (Active)" : ChatColor.RED + " (Inactive)";
				player.sendMessage(ChatColor.AQUA + "- " + arenaName + status);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}
}
