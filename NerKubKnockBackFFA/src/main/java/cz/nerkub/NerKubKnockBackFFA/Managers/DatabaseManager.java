package cz.nerkub.NerKubKnockBackFFA.Managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import cz.nerkub.NerKubKnockBackFFA.Arena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
				"rank VARCHAR(16) DEFAULT 'Unranked'" +
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

		try (Connection conn = getConnection();
			 PreparedStatement playerStatsStmt = conn.prepareStatement(playerStatsSQL);
			 PreparedStatement arenasStmt = conn.prepareStatement(arenasSQL);
			 PreparedStatement playersInArenaStmt = conn.prepareStatement(playersInArenaSQL)) {

			// Vytvoření tabulek
			playerStatsStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'player_stats' created or already exists.");

			arenasStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'arenas' created or already exists.");

			playersInArenaStmt.executeUpdate();
			plugin.getLogger().info("✅ Table 'players_in_arena' created or already exists.");

		} catch (SQLException e) {
			plugin.getLogger().severe("❌ Error creating tables!");
			e.printStackTrace();
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


	public void close() {
		if (dataSource != null) {
			dataSource.close();
			plugin.getLogger().info("❌ Database connection closed.");
		}
	}


}
