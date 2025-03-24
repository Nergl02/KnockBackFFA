package cz.nerkub.NerKubKnockBackFFA;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdatesGitHub {
	private final NerKubKnockBackFFA plugin;
	private static final String GITHUB_API_URL = "https://api.github.com/repos/Nergl02/KnockBackFFA/releases/latest";
	private String latestVersion = "Unknown";

	public CheckUpdatesGitHub(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public void checkForUpdates() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				URL url = new URL(GITHUB_API_URL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
				connection.setRequestProperty("User-Agent", "Mozilla/5.0");

				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JSONObject json = new JSONObject(response.toString());
				latestVersion = json.getString("tag_name").replaceAll("[^0-9.]", ""); // OPRAVENO: odstraní všechny nečíselné znaky na začátku

				String currentVersion = plugin.getDescription().getVersion();

				if (!currentVersion.equalsIgnoreCase(latestVersion)) {
					Bukkit.getLogger().warning("⚠️ A new version of KnockBackFFA is available! Latest: " + latestVersion + ", Current: " + currentVersion);
				} else {
					Bukkit.getLogger().info("✅ KnockBackFFA is up to date! (Version: " + currentVersion + ")");
				}

			} catch (Exception e) {
				Bukkit.getLogger().warning("⚠️ Could not check for updates: " + e.getMessage());
			}
		});
	}

	// ✅ Přidá metodu pro upozornění hráče při připojení
	public void notifyPlayerOnJoin(Player player) {
		if (!latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion())) {
			if (player.hasPermission("knbffa.admin") || player.isOp()) {
				player.sendMessage("§e⚠ KnockBackFFA Update Available! Latest: §c" + latestVersion + " §7(Current: " + plugin.getDescription().getVersion() + ")");
				player.sendMessage("§7Download at: §bhttps://www.spigotmc.org/resources/knockbackffa.122638/");
			}
		}
	}
}