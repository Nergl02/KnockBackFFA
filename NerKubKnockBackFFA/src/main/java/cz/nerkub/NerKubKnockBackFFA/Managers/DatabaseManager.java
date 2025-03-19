package cz.nerkub.NerKubKnockBackFFA.Managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import cz.nerkub.NerKubKnockBackFFA.Arena;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {

	private final NerKubKnockBackFFA plugin;
	private HikariDataSource dataSource;

	public DatabaseManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		connect();
		createTables();
	}

	public void connect() {
		FileConfiguration config = plugin.getConfig();

		String host = config.getString("database.host", "localhost");
		int port = config.getInt("database.port", 3306);
		String dbName = config.getString("database.name");
		String userName = config.getString("database.username");
		String password = config.getString("database.password");

		String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false";

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(jdbcUrl);
		hikariConfig.setUsername(userName);
		hikariConfig.setPassword(password);

		hikariConfig.setMaximumPoolSize(10);
		hikariConfig.setMinimumIdle(2);
		hikariConfig.setIdleTimeout(60000);
		hikariConfig.setMaxLifetime(1800000);
		hikariConfig.setConnectionTimeout(30000);

		dataSource = new HikariDataSource(hikariConfig);
		plugin.getLogger().info("✅ Connect to the database " + dbName + "!");

	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private void createTables() {
		String playerStatsSQL = "CREATE TABLE IF NOT EXISTS player_stats (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"name VARCHAR(16) NOT NULL, " +
				"kills INT DEFAULT 0, " +
				"deaths INT DEFAULT 0, " +
				"kd DOUBLE GENERATED ALWAYS AS (IF(deaths > 0, kills / deaths, kills)) STORED, " +
				"max_killstreak INT DEFAULT 0, " +
				"coins INT DEFAULT 0, " +
				"elo INT DEFAULT 0, " +
				"rank VARCHAR(16) DEFAULT 'Unranked' " +
				");";

		String arenasSQL = "CREATE TABLE IF NOT EXISTS arenas (" +
				"arena_name VARCHAR(64) PRIMARY KEY, " +
				"world VARCHAR(64) NOT NULL, " +
				"spawn_x DOUBLE NOT NULL, " +
				"spawn_y DOUBLE NOT NULL, " +
				"spawn_z DOUBLE NOT NULL, " +
				"spawn_yaw FLOAT NOT NULL, " +
				"spawn_pitch FLOAT NOT NULL, " +
				"min_x DOUBLE NOT NULL, " +
				"min_y DOUBLE NOT NULL, " +
				"min_z DOUBLE NOT NULL, " +
				"max_x DOUBLE NOT NULL, " +
				"max_y DOUBLE NOT NULL, " +
				"max_z DOUBLE NOT NULL, " +
				"is_active TINYINT(1) DEFAULT 0" +
				");";

		String playersInArenaSQL = "CREATE TABLE IF NOT EXISTS players_in_arena (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"arena_name VARCHAR(64) NOT NULL, " +
				"FOREIGN KEY (arena_name) REFERENCES arenas(arena_name) ON DELETE CASCADE" +
				");";

		String playerInventoriesSQL = "CREATE TABLE IF NOT EXISTS player_inventories (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"main_inventory TEXT NOT NULL, " +
				"hotbar TEXT NOT NULL, " +
				"FOREIGN KEY (uuid) REFERENCES player_stats(uuid) ON DELETE CASCADE" +
				");";

		// ✅ Tabulka `player_kits` pro zakoupené kity
		String playerKitsSQL = "CREATE TABLE IF NOT EXISTS player_kits (" +
				"uuid VARCHAR(36) NOT NULL, " +
				"kit_name VARCHAR(64) NOT NULL, " +
				"selected TINYINT(1) DEFAULT 0, " +
				"PRIMARY KEY (uuid, kit_name), " +
				"FOREIGN KEY (uuid) REFERENCES player_stats(uuid) ON DELETE CASCADE" +
				");";

		// ✅ Tabulka `player_custom_kits` pro uložení upravených kitů hráčů
		String playerCustomKitsSQL = "CREATE TABLE IF NOT EXISTS player_custom_kits (" +
				"uuid VARCHAR(36) NOT NULL, " +
				"kit_name VARCHAR(64) NOT NULL, " +
				"main_inventory TEXT NOT NULL, " +
				"hotbar TEXT NOT NULL, " +
				"helmet TEXT, " +  // Přidání sloupce pro helm
				"chestplate TEXT, " +  // Přidání sloupce pro chestplate
				"leggings TEXT, " +  // Přidání sloupce pro leggings
				"boots TEXT, " +  // Přidání sloupce pro boots
				"PRIMARY KEY (uuid, kit_name), " +
				"FOREIGN KEY (uuid) REFERENCES player_stats(uuid) ON DELETE CASCADE" +
				");";

		try (Connection conn = getConnection();
			 PreparedStatement playerStatsStmt = conn.prepareStatement(playerStatsSQL);
			 PreparedStatement arenasStmt = conn.prepareStatement(arenasSQL);
			 PreparedStatement playersInArenaStmt = conn.prepareStatement(playersInArenaSQL);
			 PreparedStatement playerInventoriesStmt = conn.prepareStatement(playerInventoriesSQL);
			 PreparedStatement playerKitsStmt = conn.prepareStatement(playerKitsSQL);
			 PreparedStatement playerCustomKitsStmt = conn.prepareStatement(playerCustomKitsSQL)) {

			// Vytvoření tabulek
			playerStatsStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'player_stats' created or already exists.");

			arenasStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'arenas' created or already exists.");

			playersInArenaStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'players_in_arena' created or already exists.");

			playerInventoriesStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'player_inventories' created or already exists.");

			playerKitsStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'player_kits' created or already exists.");

			playerCustomKitsStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'player_custom_kits' created or already exists.");

		} catch (SQLException e) {
			plugin.getLogger().severe("❌ Error creating tables!");
			e.printStackTrace();
		}
	}


	// Uložení inventáře
	public void savePlayerInventory(UUID uuid, ItemStack[] mainInventory, ItemStack[] hotbar) {
		String query = "INSERT INTO player_inventories (uuid, main_inventory, hotbar) VALUES (?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE main_inventory = ?, hotbar = ?";

		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			String serializedMainInventory = serializeInventory(mainInventory);
			String serializedHotbar = serializeInventory(hotbar);

			ps.setString(1, uuid.toString());
			ps.setString(2, serializedMainInventory);
			ps.setString(3, serializedHotbar);
			ps.setString(4, serializedMainInventory);
			ps.setString(5, serializedHotbar);

			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ItemStack[] loadMainInventory(UUID uuid) {
		String query = "SELECT main_inventory FROM player_inventories WHERE uuid = ?";
		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ItemStack[] inventory = deserializeInventory(rs.getString("main_inventory"));
				return inventory;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ItemStack[27];
	}

	public ItemStack[] loadHotbar(UUID uuid) {
		String query = "SELECT hotbar FROM player_inventories WHERE uuid = ?";

		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ItemStack[] hotbar = deserializeInventory(rs.getString("hotbar"));
				return hotbar;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new ItemStack[9];
	}

	// Serializace inventáře (libovolné velikosti)
	private String serializeInventory(ItemStack[] inventory) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut)) {

			out.writeInt(inventory.length);  // Ulož velikost pole
			for (ItemStack item : inventory) {
				// Ošetření null položek
				if (item == null) {
					item = new ItemStack(Material.AIR);  // Použij prázdný item, místo null
				}
				out.writeObject(item);
			}

			return Base64.getEncoder().encodeToString(byteOut.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	// Deserializace inventáře
	private ItemStack[] deserializeInventory(String data) {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(data));
			 BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn)) {

			int size = in.readInt();
			ItemStack[] inventory = new ItemStack[size];

			for (int i = 0; i < size; i++) {
				inventory[i] = (ItemStack) in.readObject();
			}
			return inventory;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return new ItemStack[0]; // Return empty array on error
		}
	}


	public void insertPlayer(String uuid, String name) {
		String sql = "INSERT INTO player_stats (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name);";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			stmt.setString(2, name);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateStats(String uuid, int kills, int deaths, int killstreak, int elo, int coins, String rank) {
		String sql = "UPDATE player_stats SET " +
				"kills = kills + ?, " +
				"deaths = deaths + ?, " +
				"max_killstreak = GREATEST(max_killstreak, ?), " +
				"elo = ?, " +
				"coins = ?, " +
				"rank = ? " +
				"WHERE uuid = ?;";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, kills);
			stmt.setInt(2, deaths);
			stmt.setInt(3, killstreak);
			stmt.setInt(4, elo);
			stmt.setInt(5, coins);
			stmt.setString(6, rank);
			stmt.setString(7, uuid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public PlayerStats getPlayerStats(String uuid) {
		String sql = "SELECT * FROM player_stats WHERE uuid = ?;";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return new PlayerStats(
						rs.getString("uuid"),
						rs.getString("name"),
						rs.getInt("kills"),
						rs.getInt("deaths"),
						rs.getInt("max_killstreak"),
						rs.getInt("elo"),
						rs.getInt("coins"),
						rs.getString("rank")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public void savePlayerStats(PlayerStats stats) {
		String sql = "UPDATE player_stats SET kills = ?, deaths = ?, max_killstreak = ?, elo = ?, coins = ?, rank = ? WHERE uuid = ?;";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, stats.getKills());
			stmt.setInt(2, stats.getDeaths());
			stmt.setInt(3, stats.getMaxKillstreak());
			stmt.setInt(4, stats.getElo());
			stmt.setInt(5, stats.getCoins());
			stmt.setString(6, stats.getRank());
			stmt.setString(7, stats.getUuid());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveArenaToDatabase(String arenaName, Location spawn, Location min, Location max) {
		String sql = "INSERT INTO arenas (arena_name, world, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, " +
				"min_x, min_y, min_z, max_x, max_y, max_z) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"world = VALUES(world), spawn_x = VALUES(spawn_x), spawn_y = VALUES(spawn_y), spawn_z = VALUES(spawn_z), " +
				"spawn_yaw = VALUES(spawn_yaw), spawn_pitch = VALUES(spawn_pitch), " +
				"min_x = VALUES(min_x), min_y = VALUES(min_y), min_z = VALUES(min_z), " +
				"max_x = VALUES(max_x), max_y = VALUES(max_y), max_z = VALUES(max_z);";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			stmt.setString(2, spawn.getWorld().getName());
			stmt.setDouble(3, spawn.getX());
			stmt.setDouble(4, spawn.getY());
			stmt.setDouble(5, spawn.getZ());
			stmt.setFloat(6, spawn.getYaw());
			stmt.setFloat(7, spawn.getPitch());

			stmt.setDouble(8, min.getX());
			stmt.setDouble(9, min.getY());
			stmt.setDouble(10, min.getZ());

			stmt.setDouble(11, max.getX());
			stmt.setDouble(12, max.getY());
			stmt.setDouble(13, max.getZ());

			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean removeArenaFromDatabase(String arenaName) {
		String sql = "DELETE FROM arenas WHERE arena_name = ?;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected > 0) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void updateArenaSpawn(String arenaName, Location spawn) {
		String sql = "UPDATE arenas SET spawn_x = ?, spawn_y = ?, spawn_z = ?, spawn_yaw = ?, spawn_pitch = ? WHERE arena_name = ?;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDouble(1, spawn.getX());
			stmt.setDouble(2, spawn.getY());
			stmt.setDouble(3, spawn.getZ());
			stmt.setFloat(4, spawn.getYaw());
			stmt.setFloat(5, spawn.getPitch());
			stmt.setString(6, arenaName);

			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Map<String, Arena> loadArenasFromDatabase() {
		Map<String, Arena> arenas = new HashMap<>();
		String sql = "SELECT * FROM arenas;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			int count = 0;
			while (rs.next()) {
				String arenaName = rs.getString("arena_name");
				String worldName = rs.getString("world");

				// Debug logs for each arena being loaded
				Bukkit.getLogger().info("[DB DEBUG] Found arena record: " + arenaName + ", world: " + worldName);

				if (worldName == null || Bukkit.getWorld(worldName) == null) {
					Bukkit.getLogger().warning("[DB DEBUG] World '" + worldName + "' is not loaded. Attempting to load...");
					Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));

					if (Bukkit.getWorld(worldName) == null) {
						Bukkit.getLogger().severe("[DB DEBUG] Failed to load world '" + worldName + "'. Skipping arena.");
						continue;
					}
				}

				// Spawn location
				Location spawn = new Location(Bukkit.getWorld(worldName),
						rs.getDouble("spawn_x"),
						rs.getDouble("spawn_y"),
						rs.getDouble("spawn_z"),
						rs.getFloat("spawn_yaw"),
						rs.getFloat("spawn_pitch"));

				// Arena boundaries (min and max)
				Location min = new Location(Bukkit.getWorld(worldName),
						rs.getDouble("min_x"),
						rs.getDouble("min_y"),
						rs.getDouble("min_z"));

				Location max = new Location(Bukkit.getWorld(worldName),
						rs.getDouble("max_x"),
						rs.getDouble("max_y"),
						rs.getDouble("max_z"));

				// Create arena instance
				Arena arena = new Arena(arenaName, spawn, min, max);
				arenas.put(arenaName, arena);

				Bukkit.getLogger().info("✅ [DB DEBUG] Arena '" + arenaName + "' successfully loaded.");
				count++;
			}

			Bukkit.getLogger().info("[DB DEBUG] Total arenas loaded: " + count);

		} catch (SQLException e) {
			Bukkit.getLogger().severe("[DB ERROR] Error while loading arenas from the database!");
			e.printStackTrace();
		}

		return arenas;
	}



	public boolean doesArenaExist(String arenaName) {
		String sql = "SELECT COUNT(*) FROM arenas WHERE arena_name = ?;";
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, arenaName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getCurrentArena() {
		String sql = "SELECT arena_name FROM arenas WHERE is_active = 1 LIMIT 1;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				String arenaName = rs.getString("arena_name");
				return arenaName;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean setCurrentArenaInDatabase(String arenaName) {
		String deactivateSql = "UPDATE arenas SET is_active = 0 WHERE is_active = 1;";
		String activateSql = "UPDATE arenas SET is_active = 1 WHERE arena_name = ?;";

		try (Connection conn = getConnection();
			 PreparedStatement deactivateStmt = conn.prepareStatement(deactivateSql);
			 PreparedStatement activateStmt = conn.prepareStatement(activateSql)) {

			// Deaktivuj všechny arény
			deactivateStmt.executeUpdate();

			// Aktivuj novou arénu
			activateStmt.setString(1, arenaName);
			int rowsAffected = activateStmt.executeUpdate();

			if (rowsAffected > 0) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	public void setFirstArenaActive() {
		String findSql = "SELECT arena_name FROM arenas LIMIT 1;";
		String setActiveSql = "UPDATE arenas SET is_active = 1 WHERE arena_name = ?;";

		try (Connection conn = getConnection();
			 PreparedStatement findStmt = conn.prepareStatement(findSql);
			 ResultSet rs = findStmt.executeQuery()) {

			if (rs.next()) {
				String firstArena = rs.getString("arena_name");
				try (PreparedStatement setStmt = conn.prepareStatement(setActiveSql)) {
					setStmt.setString(1, firstArena);
					setStmt.executeUpdate();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addPlayerToArena(UUID uuid, String arenaName) {
		String sql = "INSERT INTO players_in_arena (uuid, arena_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE arena_name = VALUES(arena_name);";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			stmt.setString(2, arenaName);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				plugin.getLogger().info("[DB DEBUG] Successfully added/updated player " + uuid + " in arena '" + arenaName + "'.");
			} else {
				plugin.getLogger().warning("[DB DEBUG] No rows affected for player " + uuid + ".");
			}

		} catch (SQLException e) {
			plugin.getLogger().severe("[DB ERROR] Failed to add player to arena: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void removePlayerFromArena(UUID uuid) {
		String sql = "DELETE FROM players_in_arena WHERE uuid = ?;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			stmt.executeUpdate();

			plugin.getLogger().info("[DB DEBUG] Removed player " + uuid + " from arena.");

		} catch (SQLException e) {
			plugin.getLogger().severe("❌ Error removing player from arena!");
			e.printStackTrace();
		}
	}

	public Map<UUID, String> getPlayersInArena() {
		Map<UUID, String> players = new HashMap<>();
		String sql = "SELECT uuid, arena_name FROM players_in_arena;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String arenaName = rs.getString("arena_name");
				players.put(uuid, arenaName);
			}

			plugin.getLogger().info("[DB DEBUG] Loaded " + players.size() + " players in arenas.");

		} catch (SQLException e) {
			plugin.getLogger().severe("❌ Error loading players in arena!");
			e.printStackTrace();
		}

		return players;
	}

	public List<String> getAllArenaNames() {
		List<String> arenaNames = new ArrayList<>();
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("SELECT arena_name FROM arenas");
			 ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				arenaNames.add(resultSet.getString("arena_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arenaNames;
	}

	public boolean hasKit(UUID uuid, String kitName) {
		String sql = "SELECT COUNT(*) FROM player_kits WHERE uuid = ? AND kit_name = ?;";
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			stmt.setString(2, kitName);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0; // Pokud hráč má kit, vrátíme true
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // Pokud hráč kit nemá, vrátíme false
	}

	public void addKit(UUID uuid, String kitName) {
		String sql = "INSERT INTO player_kits (uuid, kit_name, selected) VALUES (?, ?, FALSE) " +
				"ON DUPLICATE KEY UPDATE kit_name = kit_name;";

		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			stmt.setString(2, kitName);

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setSelectedKit(UUID uuid, String kitName) {
		String resetSQL = "UPDATE player_kits SET selected = 0 WHERE uuid = ?";
		String updateSQL = "UPDATE player_kits SET selected = 1 WHERE uuid = ? AND kit_name = ?";

		try (Connection conn = getConnection();
			 PreparedStatement resetStmt = conn.prepareStatement(resetSQL);
			 PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

			resetStmt.setString(1, uuid.toString());
			resetStmt.executeUpdate();

			updateStmt.setString(1, uuid.toString());
			updateStmt.setString(2, kitName);
			updateStmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getSelectedKit(UUID uuid) {
		String sql = "SELECT kit_name FROM player_kits WHERE uuid = ? AND selected = 1";
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("kit_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // Pokud hráč nemá vybraný žádný kit
	}

	// ✅ Ověření, zda hráč má vlastní kit
	public boolean hasCustomKit(UUID uuid, String kitName) {
		String sql = "SELECT COUNT(*) FROM player_custom_kits WHERE uuid = ? AND kit_name = ?;";
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid.toString());
			stmt.setString(2, kitName);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 🛡 Uložit brnění hráče do databáze
	public void saveCustomKit(UUID uuid, String kitName, ItemStack[] mainInventory, ItemStack[] hotbar, ItemStack[] armor) {
		String query = "INSERT INTO player_custom_kits (uuid, kit_name, main_inventory, hotbar, helmet, chestplate, leggings, boots) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"main_inventory = VALUES(main_inventory), " +
				"hotbar = VALUES(hotbar), " +
				"helmet = IF(VALUES(helmet) IS NOT NULL, VALUES(helmet), helmet), " +
				"chestplate = IF(VALUES(chestplate) IS NOT NULL, VALUES(chestplate), chestplate), " +
				"leggings = IF(VALUES(leggings) IS NOT NULL, VALUES(leggings), leggings), " +
				"boots = IF(VALUES(boots) IS NOT NULL, VALUES(boots), boots)";

		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			String serializedMainInventory = serializeInventory(mainInventory);
			String serializedHotbar = serializeInventory(hotbar);

			// Pokud je armor null, načteme brnění z kits.yml
			if (armor == null || armor.length != 4) {
				armor = plugin.getKitManager().getKitArmor(kitName);
			}

// Pokud i teď je armor null, nastavíme prázdné sloty
			if (armor == null) {
				armor = new ItemStack[]{null, null, null, null};
			}


			// ✅ Pokud část brnění není v kitu, neukládáme ji
			String serializedHelmet = (armor != null && armor.length > 3 && armor[3] != null) ? serializeArmor(armor[3]) : null;
			String serializedChestplate = (armor != null && armor.length > 2 && armor[2] != null) ? serializeArmor(armor[2]) : null;
			String serializedLeggings = (armor != null && armor.length > 1 && armor[1] != null) ? serializeArmor(armor[1]) : null;
			String serializedBoots = (armor != null && armor.length > 0 && armor[0] != null) ? serializeArmor(armor[0]) : null;

			ps.setString(1, uuid.toString());
			ps.setString(2, kitName);
			ps.setString(3, serializedMainInventory);
			ps.setString(4, serializedHotbar);
			ps.setString(5, serializedHelmet);
			ps.setString(6, serializedChestplate);
			ps.setString(7, serializedLeggings);
			ps.setString(8, serializedBoots);

			ps.executeUpdate();
			Bukkit.getLogger().info("[NerKubKnockBackFFA] ✅ Kit " + kitName + " uložen pro hráče " + uuid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 🛡 Načíst brnění hráče z databáze
	public ItemStack[] loadCustomKitArmor(UUID uuid, String kitName) {
		String query = "SELECT helmet, chestplate, leggings, boots FROM player_custom_kits WHERE uuid = ? AND kit_name = ?";
		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, uuid.toString());
			ps.setString(2, kitName);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// ✅ Ověříme, zda některý slot obsahuje brnění
				boolean hasArmor = false;
				ItemStack[] armor = new ItemStack[4];

				armor[3] = deserializeArmor(rs.getString("helmet"));     // Helmet
				armor[2] = deserializeArmor(rs.getString("chestplate")); // Chestplate
				armor[1] = deserializeArmor(rs.getString("leggings"));   // Leggings
				armor[0] = deserializeArmor(rs.getString("boots"));      // Boots

				Bukkit.getLogger().info("[NerKubKnockBackFFA] 🛡 Načteno brnění pro kit " + kitName + ":");
				for (ItemStack piece : armor) {
					if (piece != null) {
						hasArmor = true;
						break;
					}
				}

				// ✅ Pokud alespoň jedna část brnění existuje, vrátíme pole, jinak `null`
				return hasArmor ? armor : null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // 🔴 Pokud nic nenajdeme, vrátíme `null` místo prázdného pole!
	}

	private String serializeArmor(ItemStack item) {
		if (item == null) return "";
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut)) {

			out.writeObject(item); // Serialize item
			return Base64.getEncoder().encodeToString(byteOut.toByteArray()); // Encode to Base64
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private ItemStack deserializeArmor(String data) {
		if (data == null || data.isEmpty()) return null;
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(data));
			 BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn)) {

			return (ItemStack) in.readObject(); // Deserialize item
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	// ✅ Načtení vlastního kitu hráče
	public ItemStack[] loadCustomKit(UUID playerUUID, String kitName, boolean isHotbar) {
		ItemStack[] defaultKit = isHotbar ? new ItemStack[9] : new ItemStack[27];

		String query = "SELECT " + (isHotbar ? "hotbar" : "main_inventory") + " FROM player_custom_kits WHERE uuid = ? AND kit_name = ?";
		try (Connection conn = getConnection();
			 PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, playerUUID.toString());
			ps.setString(2, kitName);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String serialized = rs.getString(1);
				Bukkit.getLogger().info("[DEBUG] Načítám inventář pro hráče " + playerUUID + " - Kit: " + kitName);

				ItemStack[] deserializedInventory = deserializeInventory(serialized);

				// Debugging slotů
				for (int i = 0; i < deserializedInventory.length; i++) {
					Bukkit.getLogger().info("[DEBUG] Slot " + i + ": " + getItemName(deserializedInventory[i]));
				}

				return deserializedInventory;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		Bukkit.getLogger().info("[DEBUG] Hráč " + playerUUID + " nemá custom kit " + kitName + ", vracím prázdný inventář.");
		return defaultKit; // Pokud hráč nemá custom kit, vrátíme prázdný
	}

	private String getItemName(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return "EMPTY";
		}
		return item.getType().name() + " x" + item.getAmount();
	}


	// ✅ Reset upraveného kitu (smazání)
	public void deleteCustomKit(UUID uuid, String kitName) {
		String sql = "DELETE FROM player_custom_kits WHERE uuid = ? AND kit_name = ?;";
		try (Connection conn = getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid.toString());
			stmt.setString(2, kitName);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public void close() {
		if (dataSource != null) {
			dataSource.close();
			plugin.getLogger().info("❌ Database connection closed.");
		}
	}


}
