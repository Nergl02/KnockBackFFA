package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import cz.nerkub.NerKubKnockBackFFA.Arena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ArenaManager implements Listener {

	private final NerKubKnockBackFFA plugin;

	private final InventoryRestoreManager inventoryRestoreManager;

	private String currentArena;

	private final Map<UUID, Location> firstPoint = new HashMap<>();
	private final Map<UUID, Location> secondPoint = new HashMap<>();
	private final Map<String, Arena> arenas = new HashMap<>();
	private final Set<UUID> playersInArena = new HashSet<>();

	public ArenaManager(NerKubKnockBackFFA plugin, InventoryRestoreManager inventoryRestoreManager) {
		this.plugin = plugin;

		this.inventoryRestoreManager = inventoryRestoreManager;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void leaveArena(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		String arenaName = getPlayerArena(player);

		if (!arenaName.equals("Žádná aréna")) {
			playersInArena.add(player.getUniqueId());
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().info("🔄 [DEBUG] Player added to arena list for reload: " + player.getName());

			// Debug hráčova stavu
			if (player.isOnline()) {
				// TODO: DEBUG PŘIDAT
				Bukkit.getLogger().info("[DEBUG] Player is online: " + player.getName());
				plugin.getDatabaseManager().addPlayerToArena(player.getUniqueId(), arenaName);
			} else {
				// TODO: DEBUG PŘIDAT
				Bukkit.getLogger().warning("[DEBUG] Player is offline: " + player.getName());
			}

			inventoryRestoreManager.restoreInventory(player);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.player-removed").replace("%arena%", arenaName)));
		} else {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("⚠️ [DEBUG] Player tried to leave an arena, but none was found.");
		}
	}

	public void addPlayerToArena(Player player) {
		String currentArena = getCurrentArenaName();
		if (currentArena != null && !currentArena.equals("Žádná aréna")) {
			plugin.getDatabaseManager().addPlayerToArena(player.getUniqueId(), currentArena);
			playersInArena.add(player.getUniqueId());
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().info("[DEBUG] Player added to arena: " + player.getName() + " in arena: " + currentArena);
		} else {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("[DEBUG] Cannot add player to arena, no arena is active.");
		}
	}


	public void removePlayerFromArena(Player player) {
		playersInArena.remove(player.getUniqueId());
		plugin.getDatabaseManager().removePlayerFromArena(player.getUniqueId());
		// TODO: DEBUG PŘIDAT
		Bukkit.getLogger().info("[DEBUG] Player " + player.getName() + " removed from arena.");
	}


	public Set<UUID> getPlayersInArena() {
		return playersInArena;
	}


	public void setCurrentArena(String arenaName) {
		if (plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			this.currentArena = arenaName;
			plugin.getDatabaseManager().setCurrentArenaInDatabase(arenaName);
		} else {
		}
	}

	public Map<String, Arena> getArenas() {
		return arenas;
	}

	public void giveTool(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		ItemStack tool = new ItemStack(Material.GOLDEN_HOE);
		ItemMeta meta = tool.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.GOLD + "Arena Setup Tool");
			meta.setLore(Arrays.asList(ChatColor.YELLOW + "Left Click: Set first point", ChatColor.YELLOW + "Right Click: Set second point"));
			tool.setItemMeta(meta);
		}
		player.getInventory().addItem(tool);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("arena.tool")));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.GOLDEN_HOE && item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Arena Setup Tool")) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				firstPoint.put(player.getUniqueId(), event.getClickedBlock().getLocation());
				String location = formatLocation(event.getClickedBlock().getLocation());
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("arena.first-point-set").replace("%location%", location)));
				event.setCancelled(true);
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				secondPoint.put(player.getUniqueId(), event.getClickedBlock().getLocation());
				String location = formatLocation(event.getClickedBlock().getLocation());
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						prefix + plugin.getMessages().getConfig().getString("arena.second-point-set").replace("%location%", location)));
				event.setCancelled(true);
			}
		}
	}


	public void createArena(Player player, String arenaName) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");

		if (plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("arena.already-exists")
							.replace("%arena%", arenaName)));
			return;
		}

		UUID uuid = player.getUniqueId();
		if (!firstPoint.containsKey(uuid) || !secondPoint.containsKey(uuid)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("arena.points-not-set")));
			return;
		}

		Location pos1 = firstPoint.get(uuid);
		Location pos2 = secondPoint.get(uuid);

		Location min = new Location(pos1.getWorld(),
				Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));

		Location max = new Location(pos1.getWorld(),
				Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));

		Location spawn = player.getLocation();

		Arena arena = new Arena(arenaName, spawn, min, max);
		plugin.getDatabaseManager().saveArenaToDatabase(arenaName, spawn, min, max);
		arenas.put(arenaName, arena);

		firstPoint.remove(uuid);
		secondPoint.remove(uuid);

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("arena.create").replace("%arena%", arenaName)));
	}




	public boolean removeArena(Player player, String arenaName) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.invalid-arena").replace("%arena%", arenaName)));
			return false;
		}

		if (plugin.getDatabaseManager().removeArenaFromDatabase(arenaName)) {
			arenas.remove(arenaName);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.remove").replace("%arena%", arenaName)));
			return true;
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.remove-fail").replace("%arena%", arenaName)));
			return false;
		}
	}

	public void loadCurrentArena() {
		String activeArena = plugin.getDatabaseManager().getCurrentArena();
		if (activeArena != null) {
			this.currentArena = activeArena;
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().info("[DEBUG] Loaded current arena: " + currentArena);
		} else {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("[DEBUG] No current arena set.");
		}
	}



	public void loadArenas() {
		Map<String, Arena> loadedArenas = plugin.getDatabaseManager().loadArenasFromDatabase();

		if (loadedArenas.isEmpty()) {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("[DEBUG] Nebyly nalezeny žádné arény v databázi.");
			return;
		}

		arenas.clear();

		for (Arena arena : loadedArenas.values()) {
			arenas.put(arena.getName(), arena);
		}
	}


	public void setArenaSpawn(Player player, String arenaName, Location spawn) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");

		// Ověření, zda aréna existuje v databázi
		if (!plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("arena.not-exist").replace("%arena%", arenaName)));
			return;
		}

		// Aktualizace spawnu
		try {
			plugin.getDatabaseManager().updateArenaSpawn(arenaName, spawn);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					prefix + plugin.getMessages().getConfig().getString("arena.spawn-set").replace("%arena%", arenaName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void setArenaBounds(String arenaName, Location min, Location max) {
		if (!plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			return;
		}

		String sql = "UPDATE arenas SET min_x = ?, min_y = ?, min_z = ?, max_x = ?, max_y = ?, max_z = ? WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			// Nastavení hodnot pro hranice
			stmt.setDouble(1, min.getX());
			stmt.setDouble(2, min.getY());
			stmt.setDouble(3, min.getZ());
			stmt.setDouble(4, max.getX());
			stmt.setDouble(5, max.getY());
			stmt.setDouble(6, max.getZ());
			stmt.setString(7, arenaName);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public String getCurrentArenaName() {
		String dbArena = plugin.getDatabaseManager().getCurrentArena();
		return dbArena != null ? dbArena : "No arena is set";
	}


	public void switchToNextArena() {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		List<String> arenaList = new ArrayList<>(arenas.keySet());

		if (arenaList.isEmpty()) {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("[DEBUG] Žádné arény nejsou dostupné pro přepnutí.");
			return;
		}

		int currentIndex = currentArena != null ? arenaList.indexOf(currentArena) : -1;
		int nextIndex = (currentIndex + 1) % arenaList.size();
		String nextArena = arenaList.get(nextIndex);

		if (nextArena != null && !nextArena.equals(currentArena)) {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().info("🔄 [DEBUG] Switching from arena '" + currentArena + "' to arena '" + nextArena + "'.");
			setCurrentArena(nextArena);
			plugin.getDatabaseManager().setCurrentArenaInDatabase(nextArena);

			Location spawn = getArenaSpawn(nextArena);
			if (spawn != null) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.teleport(spawn);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
							plugin.getMessages().getConfig().getString("arena.switch").replace("%arena%", nextArena)));
				}
			} else {
				// TODO: DEBUG PŘIDAT
				Bukkit.getLogger().warning("⚠️ [DEBUG] Spawn pro arénu '" + nextArena + "' nebyl nalezen.");
			}
		} else {
			// TODO: DEBUG PŘIDAT
			Bukkit.getLogger().warning("⚠️ [DEBUG] Aréna se nezměnila.");
		}
	}



	public boolean isPlayerInArena(Player player) {
		String arenaName = getPlayerArena(player);
		return arenaName != null && !arenaName.equals("Žádná aréna");
	}

	public boolean doesArenaExist(String arenaName) {
		String sql = "SELECT COUNT(*) FROM arenas WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0; // Pokud je výsledek větší než 0, aréna existuje
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}


	public void joinCurrentArena(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (currentArena == null || currentArena.equals("Žádná aréna")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("arena.no-set")));
			return;
		}

		Location spawn = getArenaSpawn(currentArena);
		if (spawn != null) {
			player.teleport(spawn);
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("arena.no-spawn").replace("%arena%", currentArena)));
		}
	}


	public Location getArenaSpawn(String arenaName) {
		if (!plugin.getDatabaseManager().doesArenaExist(arenaName)) {
			return null;
		}

		String sql = "SELECT world, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch FROM arenas WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String worldName = rs.getString("world");
				World world = Bukkit.getWorld(worldName);

				if (world == null) {
					return null;
				}

				double x = rs.getDouble("spawn_x");
				double y = rs.getDouble("spawn_y");
				double z = rs.getDouble("spawn_z");
				float yaw = rs.getFloat("spawn_yaw");
				float pitch = rs.getFloat("spawn_pitch");

				return new Location(world, x, y, z, yaw, pitch);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}


	public void teleportPlayersToRandomArena() {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		List<String> arenaNames = new ArrayList<>(arenas.keySet()); // Použij seznam načtených arén z paměti

		if (arenaNames.isEmpty()) {
			return;
		}

		String randomArena = arenaNames.get(new Random().nextInt(arenaNames.size()));
		setCurrentArena(randomArena);

		Location spawn = getArenaSpawn(randomArena);
		if (spawn != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.teleport(spawn);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("arena.teleport").replace("%arena%", currentArena)));
			}
		}
	}


	public String getPlayerArena(Player player) {
		if (player == null || player.getLocation() == null) {
			return null;
		}

		Location playerLocation = player.getLocation();

		for (Arena arena : arenas.values()) {
			Location min = arena.getMinBounds();
			Location max = arena.getMaxBounds();

			if (min != null && max != null && isInsideBounds(playerLocation, min, max)) {
				return arena.getName();
			}
		}

		return "Žádná aréna";
	}





	private boolean isInsideBounds(Location loc, Location min, Location max) {
		return loc.getX() >= min.getX() && loc.getX() <= max.getX()
				&& loc.getY() >= min.getY()
				&& loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
	}

	private String formatLocation(Location loc) {
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}

	public Location getArenaMinBounds(String arenaName) {
		String sql = "SELECT min_x, min_y, min_z, world FROM arenas WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				World world = Bukkit.getWorld(rs.getString("world"));
				if (world == null) return null;

				return new Location(world, rs.getDouble("min_x"), rs.getDouble("min_y"), rs.getDouble("min_z"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Location getArenaMaxBounds(String arenaName) {
		String sql = "SELECT max_x, max_y, max_z, world FROM arenas WHERE arena_name = ?;";

		try (Connection conn = plugin.getDatabaseManager().getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				World world = Bukkit.getWorld(rs.getString("world"));
				if (world == null) return null;

				return new Location(world, rs.getDouble("max_x"), rs.getDouble("max_y"), rs.getDouble("max_z"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
