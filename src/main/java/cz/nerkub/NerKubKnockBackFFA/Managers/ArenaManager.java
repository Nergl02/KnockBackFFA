package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ArenaManager {

	private final NerKubKnockBackFFA plugin;
	private final ScoreboardUpdater scoreboardUpdater;
	private final Random random;
	private int timeRemaining;
	private final InventoryManager inventoryManager;

	private String currentArena;
	private String lastArena;

	public ArenaManager(NerKubKnockBackFFA plugin, ScoreboardUpdater scoreboardUpdater, Random random, InventoryManager inventoryManager) {
		this.plugin = plugin;
		this.scoreboardUpdater = scoreboardUpdater;
		this.random = random;
		this.inventoryManager = inventoryManager;
	}

	public void setCurrentArena(String arenaName) {
		currentArena = arenaName; // Nastav aktuální arénu
		Bukkit.getLogger().info("Aktuální aréna byla nastavena na: " + currentArena);

		// Získej lokaci arény ze souboru s konfigurací
		Location arenaSpawn = getArenaSpawn(arenaName);

		if (arenaSpawn == null) {
			Bukkit.getLogger().warning("Arena spawn location is null for arena: " + arenaName);
			return; // Pokud je spawn null, ukonči metodu
		}

		// Vytvoř bezpečnostní zónu na základě arény
		createSafeZone(arenaSpawn);
	}

	public String getCurrentArena() {
		return currentArena;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	// Teleport hráče do aktuální arény
	public void teleportPlayerToCurrentArena(Player player) {
		// Zkontroluj, zda je nastavena aktuální aréna
		if (currentArena == null) {
			Bukkit.getLogger().warning("Žádná aktivní aréna není nastavena pro hráče: " + player.getName());
			return; // Pokud není nastavena aréna, ukonči metodu
		}

		// Získej lokaci spawnu pro aktuální arénu
		World world = Bukkit.getWorld(plugin.getArenas().getConfig().getString(currentArena + ".spawn.world"));
		double x = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.x");
		double y = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.y");
		double z = plugin.getArenas().getConfig().getDouble(currentArena + ".spawn.z");

		// Zkontroluj, zda svět existuje
		if (world == null) {
			Bukkit.getLogger().warning("Svět pro arénu " + currentArena + " nebyl nalezen pro hráče: " + player.getName() + "!");
			return; // Pokud svět neexistuje, ukonči metodu
		}

		// Vytvoř lokaci pro teleportaci
		Location spawnLocation = new Location(world, x, y, z);
		player.teleport(spawnLocation); // Teleportuj hráče
		Bukkit.getLogger().info("Hráč " + player.getName() + " byl teleportován do arény: " + currentArena + " na pozici: " + spawnLocation.toString());

		// Vytvoř bezpečnostní zónu pod hráčem
		createSafeZone(spawnLocation);
	}

	private void createSafeZone(Location location) {
		if (location == null) {
			Bukkit.getLogger().warning("Location is null! Cannot create safe zone.");
			return; // Ukonči metodu, pokud je location null
		}

		// Definuj vzdálenost pro bezpečnostní zónu
		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius"); // Můžeš změnit podle potřeby

		// Vytvoř bezpečnostní zónu
		for (int x = -safeZoneRadius; x <= safeZoneRadius; x++) {
			for (int z = -safeZoneRadius; z <= safeZoneRadius; z++) {
				Location safeZoneLocation = location.clone().add(x, 0, z);
			}
		}
	}

	public void teleportPlayersToRandomArena() {
		Set<String> arenas = plugin.getArenas().getConfig().getKeys(false);
		if (arenas.isEmpty()) {
			Bukkit.getLogger().warning("There are no arenas in arena.yml!");
			return;
		}

		List<String> arenaList = new ArrayList<>(arenas);
		String randomArena;

		if (arenaList.size() == 1) {
			// Pokud je pouze jedna aréna, nastav ji jako aktuální arénu
			randomArena = arenaList.get(0);
			setCurrentArena(randomArena);
			lastArena = randomArena; // Aktualizuj lastArena
		} else {
			// Vybírej náhodně, pokud je více než jedna aréna
			do {
				randomArena = arenaList.get(random.nextInt(arenaList.size()));
			} while (randomArena.equals(lastArena)); // Zajistí, že se nevybere stejná aréna jako poslední

			setCurrentArena(randomArena); // Nastav aktuální arénu
			lastArena = randomArena; // Aktualizuj lastArena
		}


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
			plugin.getScoreBoardManager().startScoreboardUpdater(player);
		}
	}

	public Location getArenaSpawn(String arenaName) {

		if (arenaName == null || arenaName.isEmpty()) {
			Bukkit.getLogger().warning("Název arény je null nebo prázdný!");
			return null; // nebo to ošetři jinak
		}
		// Získej konfiguraci arény
		String worldName = plugin.getArenas().getConfig().getString(arenaName + ".spawn.world");
		double x = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.x");
		double y = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.y");
		double z = plugin.getArenas().getConfig().getDouble(arenaName + ".spawn.z");

		// Získej svět
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			Bukkit.getLogger().warning("Svět pro arénu " + arenaName + " nebyl nalezen!");
			return null; // nebo můžeš vrátit výchozí hodnotu
		}

		// Vrať lokaci spawnu
		return new Location(world, x, y, z);
	}

	public void loadArenas() {
		// Předpoklad: arenas.yml je načten
		for (String arenaName : plugin.getArenas().getConfig().getKeys(false)) {
			Bukkit.getLogger().info("Načítání arény: " + arenaName);
			// Další kód pro načítání arény...
		}
	}

	public String getArenaOfPlayer(Player player) {
		// Pokud je hráč v aktuální aréně, vrátí její název
		if (currentArena != null && player.getWorld().getName().equals(getArenaWorldName(currentArena))) {
			return currentArena;
		}
		return null; // Pokud hráč není v žádné aréně, vrátí null
	}

	private String getArenaWorldName(String arenaName) {
		return plugin.getArenas().getConfig().getString(arenaName + ".spawn.world");
	}

	public void removePlayerFromArena(Player player) {

		if (!plugin.getConfig().getBoolean("bungee-mode")) {
			// Zkontroluj, jestli je hráč v aktuální aréně
			if (currentArena != null && player.getWorld().getName().equals(getArenaWorldName(currentArena))) {
				// Proveď jakoukoli čistící logiku, například teleportuj hráče mimo arénu
				inventoryManager.restoreLocation(player); // Teleportuj hráče ven z arény
				Bukkit.getLogger().info("Hráč " + player.getName() + " byl odstraněn z arény: " + currentArena);
			}
		}

	}

	public boolean isPlayerInArena(Player player) {
		String arena = plugin.getArenaManager().getArenaOfPlayer(player);
		if (arena == null) {
			return false;
		}
		return true;
	}

	public void leaveArena(Player player) {
		// Obnovíš inventář a pozici hráče
		inventoryManager.restoreInventory(player);
		inventoryManager.restoreLocation(player);

		// Oznámíš hráči, že opustil arénu (můžeš přidat vlastní zprávu)
		player.sendMessage(ChatColor.GREEN + "Opustil jsi arénu.");

		// Provádíš další potřebné operace pro opuštění arény (např. odstranění z arény)
		plugin.getArenaManager().removePlayerFromArena(player);
	}

}
