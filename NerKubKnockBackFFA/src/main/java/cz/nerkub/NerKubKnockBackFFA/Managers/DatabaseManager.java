package cz.nerkub.NerKubKnockBackFFA.Managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		plugin.getLogger().info("✅ Připojeno k databázi " + dbName + "!");

	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private void createTables() {
		String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"name VARCHAR(16) NOT NULL, " +
				"kills INT DEFAULT 0, " +
				"deaths INT DEFAULT 0, " +
				"kd DOUBLE GENERATED ALWAYS AS (IF(deaths > 0, kills / deaths, kills)) STORED, " +
				"max_killstreak INT DEFAULT 0, " +
				"coins INT DEFAULT 0, " +
				"elo INT DEFAULT 0, " +
				"rank VARCHAR(16) DEFAULT 'Unranked'"+
				");";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.executeUpdate();
			plugin.getLogger().info("✅ Tabulka player_stats byla vytvořena nebo již existuje.");
		} catch (SQLException e) {
			plugin.getLogger().severe("❌ Chyba při vytváření tabulky player_stats!");
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
				"coins = ?, " + // ✅ Opraveno
				"rank = ? " +
				"WHERE uuid = ?;";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, kills);
			stmt.setInt(2, deaths);
			stmt.setInt(3, killstreak);
			stmt.setInt(4, elo);
			stmt.setInt(5, coins); // ✅ Coins správně přidány
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
						rs.getInt("coins"), // ✅ Coins přidány
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
			stmt.setInt(5, stats.getCoins()); // ✅ Coins opraveny
			stmt.setString(6, stats.getRank());
			stmt.setString(7, stats.getUuid());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public void close() {
		if (dataSource != null) {
			dataSource.close();
			plugin.getLogger().info("❌ Databázové připojení uzavřeno.");
		}
	}


}
