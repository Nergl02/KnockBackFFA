package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ArenaManager {

	private final NerKubKnockBackFFA plugin;
	private final ScoreboardUpdater scoreboardUpdater;
	private final Random random;
	private int timeRemaining;

	private String currentArena;
	private String lastArena;

	public ArenaManager(NerKubKnockBackFFA plugin, ScoreboardUpdater scoreboardUpdater, Random random) {
		this.plugin = plugin;
		this.scoreboardUpdater = scoreboardUpdater;
		this.random = random;
	}

	public void setCurrentArena(String arenaName) {
		this.currentArena = arenaName;
	}

	public String getCurrentArena() {
		return currentArena;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	// Teleport hráče do aktuální arény
	public void teleportPlayerToCurrentArena(Player player) {
		if (currentArena == null) {
			player.sendMessage("Žádná aktivní aréna není nastavena.");
			return;
		}

		World world = Bukkit.getWorld(plugin.getArenas().getConfig().getString(currentArena + ".spawn.world"));
		double x = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.x");
		double y = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.y");
		double z = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.z");

		if (world == null) {
			player.sendMessage("Svět pro arénu " + currentArena + " nebyl nalezen.");
			return;
		}

		Location spawnLocation = new Location(world, x, y, z);
		player.teleport(spawnLocation);
		player.sendMessage("Byl jsi teleportován do aktivní arény: " + currentArena);
	}

	public void teleportPlayersToRandomArena() {
		Set<String> arenas = plugin.getArenas().getConfig().getKeys(false);
		if (arenas.isEmpty()) {
			Bukkit.getLogger().warning("There are no arenas in arena.yml!");
			return;
		}

		List<String> arenaList = new ArrayList<>(arenas);
		String randomArena;

		do {
			randomArena = arenaList.get(random.nextInt(arenaList.size()));
		} while (randomArena.equals(lastArena)); // Zajistí, že se nevybere stejná aréna jako poslední

		setCurrentArena(randomArena); // Nastav aktuální arénu
		lastArena = randomArena;

		Bukkit.getLogger().info("Vybrána náhodná aréna: " + randomArena);

		// Načti pozici spawnu z arenas.yml
		World world = Bukkit.getWorld(plugin.getArenas().getConfig().getString(randomArena + ".spawn.world"));
		double x = plugin.getArenas().getConfig().getDouble(randomArena + ".spawn.x");
		double y = plugin.getArenas().getConfig().getDouble(randomArena + ".spawn.y");
		double z = plugin.getArenas().getConfig().getDouble(randomArena + ".spawn.z");

		if (world == null) {
			Bukkit.getLogger().warning("Svět pro arénu " + randomArena + " nebyl nalezen!");
			return;
		}

		Location spawnLocation = new Location(world, x, y, z);

		// Teleportuj všechny hráče do vybrané arény
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(spawnLocation);
			player.sendMessage("Byl jsi teleportován do arény: " + randomArena);
			plugin.getScoreBoardManager().startScoreboardUpdater(player);
		}
	}

}
