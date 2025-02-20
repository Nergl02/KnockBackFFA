package cz.nerkub.NerKubKnockBackFFA;

import cz.nerkub.NerKubKnockBackFFA.CustomFiles.CustomConfig;
import cz.nerkub.NerKubKnockBackFFA.Expansions.KnockBackPlaceholderExpansion;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.DeathsMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Items.*;
import cz.nerkub.NerKubKnockBackFFA.Listeners.*;
import cz.nerkub.NerKubKnockBackFFA.Managers.*;
import cz.nerkub.NerKubKnockBackFFA.SubCommands.ShopSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public final class NerKubKnockBackFFA extends JavaPlugin {

	private static NerKubKnockBackFFA plugin;
	private ArenaManager arenaManager;
	private Random random;
	private int timeRemaining;

	private DatabaseManager databaseManager;

	private CustomConfig messages;
	private CustomConfig arenas;
	private CustomConfig items;
	private CustomConfig shop;
	private CustomConfig ranks;

	private PlayerStatsManager playerStatsManager;
	private final DamagerMap damagerMap = new DamagerMap(); //Nejlepší řešení místo getInstance();
	private final KillStreakMap killStreakMap = new KillStreakMap();
	private final DeathsMap deathsMap = new DeathsMap();
	private final KillsMap killsMap = new KillsMap(this);
	private final KnockBackStickItem knockBackStickItem = new KnockBackStickItem(this);
	private final PunchBowItem punchBowItem = new PunchBowItem(this);
	private final LeatherTunicItem leatherTunicItem = new LeatherTunicItem(this);
	private final BuildBlockItem buildBlockItem = new BuildBlockItem(this);
	private RankManager rankManager;  // Bez "final"
	private final LevitationBootsItem levitationBootsItem = new LevitationBootsItem(this);
	private final SwapperBallItem swapperBallItem = new SwapperBallItem(this);
	private final InvisibilityCloakItem invisibilityCloakItem = new InvisibilityCloakItem(this);
	private final FireBallLauncherItem fireBallLauncherItem = new FireBallLauncherItem(this);
	private final ExplodingChickItem explodingChickItem = new ExplodingChickItem(this);
	private final ShopManager shopManager = new ShopManager(this, levitationBootsItem, swapperBallItem, invisibilityCloakItem, fireBallLauncherItem, explodingChickItem, playerStatsManager);
	private InventoryManager inventoryManager = new InventoryManager();
	private final MaxItemInInvListener maxItemInInvListener = new MaxItemInInvListener(this);
	private final PlayerMenuManager playerMenuManager = new PlayerMenuManager(this);
	private DoubleJumpListener doubleJumpListener;

	private ScoreBoardManager scoreBoardManager;
	private ScoreboardUpdater scoreboardUpdater;


	@Override
	public void onEnable() {
		// Plugin startup logic

		saveDefaultConfig();
		reloadConfig();
		// Custom ConfigFiles
		messages = new CustomConfig("messages", "messages.yml", this); // Directory can be "" to create file in the main plugin folder
		messages.saveConfig();
		arenas = new CustomConfig("arenas", "arenas.yml", this); // Directory can be "" to create file in the main plugin folder
		arenas.saveConfig();
		items = new CustomConfig("items", "items.yml", this);
		items.saveConfig();
		shop = new CustomConfig("shop", "shop.yml", this);
		shop.saveConfig();
		ranks = new CustomConfig("ranks", "ranks.yml", this);
		ranks.saveConfig();

		plugin = this;
		random = new Random();
		arenaManager = new ArenaManager(this, scoreboardUpdater, random, inventoryManager);
		scoreBoardManager = new ScoreBoardManager(this);
		timeRemaining = plugin.getConfig().getInt("arena-time") * 60; // Převedeno na sekundy
		doubleJumpListener = new DoubleJumpListener(this);
		this.databaseManager = new DatabaseManager(this);
		this.playerStatsManager = new PlayerStatsManager(databaseManager);
		this.rankManager = new RankManager(this);

		// Otestuj připojení
		try (Connection conn = databaseManager.getConnection()) {
			if (conn != null && !conn.isClosed()) {
				getLogger().info("✅ Databáze je připravena!");
			}
		} catch (SQLException e) {
			getLogger().severe("❌ Chyba při připojování k databázi!");
			e.printStackTrace();
		}


		Bukkit.getConsoleSender().sendMessage("");
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|\\   |  | /	&aPlugin: &6NerKub KnockBackFFA"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| \\  |  |/	&aVersion: &bv1.0.0"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|  \\ |  |\\	&aAuthor: &3NerKub Studio"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|   \\|  | \\	&aPremium: &bThis plugin is a premium resource."));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| Visit our Discord for more! &ahttps://discord.gg/YXm26egK6g"));
		Bukkit.getConsoleSender().sendMessage("");

		getServer().getPluginManager().registerEvents(new BlockBreakListener(this, arenaManager), this);
		getServer().getPluginManager().registerEvents(new FallDamageListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, new Random(), databaseManager, damagerMap, killStreakMap, deathsMap, buildBlockItem, arenaManager, rankManager,
				knockBackStickItem, punchBowItem, leatherTunicItem, maxItemInInvListener), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, knockBackStickItem, punchBowItem, leatherTunicItem, buildBlockItem, arenaManager, scoreBoardManager, databaseManager, damagerMap,
				killStreakMap, killsMap, rankManager, inventoryManager), this);
		getServer().getPluginManager().registerEvents(new SwapperBallListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new DropItemListener(this, arenaManager), this);
		getServer().getPluginManager().registerEvents(new CancelBlockDestroyListener(this, arenaManager), this);
		getServer().getPluginManager().registerEvents(new SafeZoneListener(this, arenaManager), this);
		getServer().getPluginManager().registerEvents(new LevitationBootsListener(this), this);
		getServer().getPluginManager().registerEvents(new ShopBuyListener(this, shopManager), this);
		getServer().getPluginManager().registerEvents(new InvisibilityCloakListener(this), this);
		getServer().getPluginManager().registerEvents(new ArmorInteractListener(this), this);
		getServer().getPluginManager().registerEvents(new MaxItemInInvListener(this), this);
		getServer().getPluginManager().registerEvents(new FireBallLauncherListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new ExplodingChickListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(doubleJumpListener, this);


		getCommand("knbffa").setExecutor(new CommandManager(this, scoreBoardManager, shopManager, arenaManager, knockBackStickItem, punchBowItem, leatherTunicItem, buildBlockItem, rankManager, inventoryManager,
				playerMenuManager, doubleJumpListener, killsMap, damagerMap));


		new BukkitRunnable() {
			@Override
			public void run() {

				if (timeRemaining <= 0) {
					arenaManager.teleportPlayersToRandomArena();
					timeRemaining = plugin.getConfig().getInt("arena-time") * 60; // Obnovení na nastavený interval
				} else {
					timeRemaining--; // Snížení zbývajícího času každou sekundu
				}

			}

		}.runTaskTimer(this, 0, 20L); // Každou sekundu (20 ticků)

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
			new KnockBackPlaceholderExpansion(this, killStreakMap, databaseManager).register(); //
		}

		// Zkontrolujte, zda je nějaká aréna nastavena
		if (arenaManager.getCurrentArena() == null) {
			arenaManager.teleportPlayersToRandomArena();
		}

		loadCurrentArena();

		Metrics metrics = new Metrics(this, 24813);

	}

	private void loadCurrentArena() {
		Set<String> arenas = getArenas().getConfig().getKeys(false);
		if (arenas.isEmpty()) {
			getLogger().warning("Žádné arény nebyly nalezeny v arenas.yml!");
			return; // Pokud nejsou arény, ukonči metodu
		}

		// Nastav náhodnou arénu, nebo můžeš použít konkrétní logiku pro výběr
		String firstArena = arenas.iterator().next(); // Získej první arénu
		arenaManager.setCurrentArena(firstArena); // Nastav aktuální arénu
		getLogger().info("Aktuální aréna byla nastavena na: " + firstArena);
	}

	public String formatTime(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		return String.format("%dm %ds", minutes, seconds);
	}

	@Override
	public void onDisable() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			// Zkontroluj, jestli je hráč v aréně
			if (arenaManager.isPlayerInArena(player)) {
				// Simuluj volání metody /knbffa leave
				arenaManager.leaveArena(player);
			}
			inventoryManager.saveInventory(player);
			inventoryManager.saveLocation(player);
		}

		plugin.getArenas().saveConfig();
		plugin.getMessages().saveConfig();
		plugin.saveConfig();
		plugin.getItems().saveConfig();
		plugin.getShop().saveConfig();
		plugin.getRanks().saveConfig();



		if (databaseManager != null) {
			databaseManager.close();
		}

	}


	public static NerKubKnockBackFFA getPlugin() {
		return plugin;
	}

	public CustomConfig getMessages() {
		return messages;
	}

	public CustomConfig getArenas() {
		return arenas;
	}

	public CustomConfig getItems() {
		return items;
	}

	public CustomConfig getShop() {
		return shop;
	}

	public CustomConfig getRanks() {
		return ranks;
	}

	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	public ScoreBoardManager getScoreBoardManager() {
		return scoreBoardManager;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public PlayerStatsManager getPlayerStatsManager() {
		return playerStatsManager;
	}


}
