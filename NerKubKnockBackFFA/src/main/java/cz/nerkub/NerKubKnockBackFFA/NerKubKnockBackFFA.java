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
import cz.nerkub.NerKubKnockBackFFA.TabCompleters.KnbffaTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.print.PrinterAbortException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public final class NerKubKnockBackFFA extends JavaPlugin {

	private static NerKubKnockBackFFA plugin;
	private ArenaManager arenaManager;
	private Random random;
	private int timeRemaining;

	private DatabaseManager databaseManager;
	private CheckUpdatesGitHub checkUpdatesGitHub;

	private CustomConfig messages;
	private CustomConfig items;
	private CustomConfig shop;
	private CustomConfig ranks;
	private CustomConfig menu;
	private CustomConfig events;
	private CustomConfig kits;

	private PlayerStatsManager playerStatsManager;
	private SafeZoneManager safeZoneManager;
	private KitManager kitManager;
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
	private final BlazingDashItem blazingDashItem = new BlazingDashItem(this);
	private final SpeedBoostItem speedBoostItem = new SpeedBoostItem(this);
	private ShopManager shopManager;
	private InventoryRestoreManager inventoryRestoreManager = new InventoryRestoreManager();
	private final MaxItemInInvListener maxItemInInvListener = new MaxItemInInvListener(this);
	private DoubleJumpListener doubleJumpListener;
	private BlazingDashListener blazingDashListener;
	private BossBarManager bossBarManager;
	private CustomEventManager customEventManager;

	private ScoreBoardManager scoreBoardManager;
	private ScoreBoardUpdater scoreboardUpdater;


	@Override
	public void onEnable() {
		// Plugin startup logic

		saveDefaultConfig();
		reloadConfig();
		// Custom ConfigFiles
		messages = new CustomConfig("messages", "messages.yml", this); // Directory can be "" to create file in the main plugin folder
		messages.saveConfig();
		items = new CustomConfig("items", "items.yml", this);
		items.saveConfig();
		shop = new CustomConfig("inventories", "shop.yml", this);
		shop.saveConfig();
		ranks = new CustomConfig("ranks", "ranks.yml", this);
		ranks.saveConfig();
		menu = new CustomConfig("inventories", "menu.yml", this);
		menu.saveConfig();
		events = new CustomConfig("events", "events.yml", this);
		kits = new CustomConfig("kits", "kits.yml", this);
		kits.saveConfig();

		updateMainConfig();
		messages.updateConfig();
		items.updateConfig();
		shop.updateConfig();
		ranks.updateConfig();
		menu.updateConfig();
		events.updateConfig();
		kits.updateConfig();

		checkUpdatesGitHub = new CheckUpdatesGitHub(this);
		checkUpdatesGitHub.checkForUpdates();

		plugin = this;
		random = new Random();
		arenaManager = new ArenaManager(this, inventoryRestoreManager);
		scoreBoardManager = new ScoreBoardManager(this);
		timeRemaining = plugin.getConfig().getInt("arena-time") * 60; // Převedeno na sekundy
		doubleJumpListener = new DoubleJumpListener(this);
		this.databaseManager = new DatabaseManager(this);
		this.playerStatsManager = new PlayerStatsManager(databaseManager);
		this.rankManager = new RankManager(this);
		bossBarManager = new BossBarManager(this);
		ShopManager shopManager = new ShopManager(this, levitationBootsItem, swapperBallItem, invisibilityCloakItem, fireBallLauncherItem,
				explodingChickItem, blazingDashItem, speedBoostItem, playerStatsManager);
		DefaultInventoryManager defaultInventoryManager = new DefaultInventoryManager(this, databaseManager);
		InventoryMenuManager inventoryMenumanager = new InventoryMenuManager(this, databaseManager, defaultInventoryManager);
		KitMenuManager kitMenuManager = new KitMenuManager(this, databaseManager);
		PlayerMenuManager playerMenuManager = new PlayerMenuManager(this, inventoryMenumanager, kitMenuManager);
		this.safeZoneManager = new SafeZoneManager(this, arenaManager, playerMenuManager, shopManager, kitManager, kitMenuManager, databaseManager, defaultInventoryManager, leatherTunicItem, knockBackStickItem);
		this.kitManager = new KitManager(this, defaultInventoryManager);
		customEventManager = new CustomEventManager(this, safeZoneManager, arenaManager, knockBackStickItem);

		// Otestuj připojení
		try (Connection conn = databaseManager.getConnection()) {
			if (conn != null && !conn.isClosed()) {
				getLogger().info("✅ Database is ready!");
			}
		} catch (SQLException e) {
			getLogger().severe("❌ Error while connecting to the database!");
			e.printStackTrace();
		}


		Bukkit.getConsoleSender().sendMessage("");
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|\\   |  | /	&aPlugin: &6NerKub KnockBackFFA"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| \\  |  |/	&aVersion: &bv2.0.1"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|  \\ |  |\\	&aAuthor: &3NerKub Studio"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|   \\|  | \\	&aPremium: &bThis plugin is a premium resource."));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| Visit our Discord for more! &ahttps://discord.gg/YXm26egK6g"));
		Bukkit.getConsoleSender().sendMessage("");

		getServer().getPluginManager().registerEvents(new BlockBreakListener(this, arenaManager), this);
		getServer().getPluginManager().registerEvents(new FallDamageListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, new Random(), databaseManager, damagerMap, killStreakMap, deathsMap, buildBlockItem, arenaManager, rankManager,
				knockBackStickItem, punchBowItem, leatherTunicItem, maxItemInInvListener, defaultInventoryManager), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, arenaManager, scoreBoardManager, databaseManager, defaultInventoryManager, checkUpdatesGitHub, damagerMap,
				killStreakMap, killsMap, rankManager, inventoryRestoreManager), this);
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
		getServer().getPluginManager().registerEvents(new BlazingDashListener(this), this);
		getServer().getPluginManager().registerEvents(doubleJumpListener, this);
		getServer().getPluginManager().registerEvents(new SpeedBoostListener(this), this);
		getServer().getPluginManager().registerEvents(inventoryMenumanager, this);
		getServer().getPluginManager().registerEvents(playerMenuManager, this);
		getServer().getPluginManager().registerEvents(new KitMenuManager(this, databaseManager), this);
		getServer().getPluginManager().registerEvents(new SafeZoneManager(this, arenaManager, playerMenuManager, shopManager, kitManager ,kitMenuManager, databaseManager, defaultInventoryManager, leatherTunicItem, knockBackStickItem), this);

		getCommand("knbffa").setExecutor(new CommandManager(this, scoreBoardManager, shopManager, arenaManager, knockBackStickItem, punchBowItem, leatherTunicItem, buildBlockItem, rankManager, inventoryRestoreManager,
				playerMenuManager, doubleJumpListener, blazingDashListener, killsMap, damagerMap));
		getCommand("knbffa").setTabCompleter(new KnbffaTabCompleter(databaseManager));

		arenaManager.loadArenas();
		arenaManager.loadCurrentArena();

		if (getConfig().getBoolean("boss-bar", true)) {
			bossBarManager.updateBossBar();
		}


		// Načti aktuální aktivní arénu
		String currentArena = plugin.getDatabaseManager().getCurrentArena();
		if (currentArena != null) {
			arenaManager.setCurrentArena(currentArena);
			Bukkit.getLogger().info("✅ [DB DEBUG] Current arena at startup: " + currentArena);
		} else {
			Bukkit.getLogger().warning("⚠️ [DB DEBUG] No arena is currently set as active.");
			// Pokud není aktivní žádná aréna, nastav první dostupnou
			plugin.getDatabaseManager().setFirstArenaActive();

			// Po nastavení znovu ověř aktivní arénu
			String newCurrentArena = plugin.getDatabaseManager().getCurrentArena();
			if (newCurrentArena != null) {
				arenaManager.setCurrentArena(newCurrentArena);
				Bukkit.getLogger().info("✅ [DB DEBUG] Automatically set arena: " + newCurrentArena);
			} else {
				Bukkit.getLogger().warning("⚠️ [DB DEBUG] Even after automatic setting, no arena is active.");
			}

		}



		new BukkitRunnable() {
			@Override
			public void run() {
				if (timeRemaining <= 0) {
					Bukkit.getLogger().info("[DEBUG] Time is over, switching to a new arena...");
					arenaManager.switchToNextArena();

					if (bossBarManager != null) {
						Bukkit.getLogger().info("[DEBUG] Reseting BossBar.");
						bossBarManager.resetBossBar();
					}

					timeRemaining = plugin.getConfig().getInt("arena-time") * 60;
				} else {
					timeRemaining--;
					if (bossBarManager != null) {
						bossBarManager.setTimeRemaining(timeRemaining);
					}
				}
			}
		}.runTaskTimer(this, 0, 20L);


		new BukkitRunnable() {
			@Override
			public void run() {
				Map<UUID, String> players = plugin.getDatabaseManager().getPlayersInArena();

				for (Map.Entry<UUID, String> entry : players.entrySet()) {
					UUID uuid = entry.getKey();
					String arenaName = entry.getValue();

					Player player = Bukkit.getPlayer(uuid);
					if (player != null && player.isOnline()) {
						Location spawn = plugin.getArenaManager().getArenaSpawn(arenaName);
						if (spawn != null) {
							player.teleport(spawn);
							plugin.getArenaManager().addPlayerToArena(player);
							Bukkit.getLogger().info("[DEBUG] Teleported " + player.getName() + " to arena '" + arenaName + "'.");
						} else {
							Bukkit.getLogger().warning("[DEBUG] Spawn not found for arena '" + arenaName + "'.");
						}
					}
				}
			}
		}.runTaskLater(this, 40L);

		// Registrace PlaceholderAPI, pokud je povolena
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new KnockBackPlaceholderExpansion(this, killStreakMap, databaseManager).register();
			Bukkit.getLogger().info("PlaceholderAPI úspěšně registrována.");
		} else {
			Bukkit.getLogger().warning("PlaceholderAPI není dostupná.");
		}

		// Zkontrolujte, zda je nastavena aréna, pokud ne, teleportuj hráče do náhodné
		if (arenaManager.getCurrentArenaName().equals("Žádná aréna")) {
			arenaManager.teleportPlayersToRandomArena();
			Bukkit.getLogger().info("Žádná aréna nebyla nastavena, teleportuji hráče do náhodné arény.");
		} else {
			Bukkit.getLogger().info("Aktuální aréna: " + arenaManager.getCurrentArenaName());
		}

		Metrics metrics = new Metrics(this, 24813);

	}


	public String formatTime(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		return String.format("%dm %ds", minutes, seconds);
	}

	@Override
	public void onDisable() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (arenaManager.isPlayerInArena(player)) {
				arenaManager.leaveArena(player);
			}
		}

		plugin.getMessages().saveConfig();
		plugin.saveConfig();
		plugin.getItems().saveConfig();
		plugin.getShop().saveConfig();
		plugin.getRanks().saveConfig();
		plugin.getMenu().saveConfig();
		plugin.getEvents().saveConfig();
		plugin.getKits().saveConfig();

		if (databaseManager != null) {
			databaseManager.close();
		}

		if (bossBarManager != null) {
			bossBarManager.removeBossBar();
		}

		String arenaName = arenaManager.getCurrentArenaName();
		if (arenaName != null && !arenaName.equals("No arena is set")) {
			Location min = arenaManager.getArenaMinBounds(arenaName);
			Location max = arenaManager.getArenaMaxBounds(arenaName);

			if (min != null && max != null) {
				World world = min.getWorld();

				for (org.bukkit.entity.Arrow arrow : world.getEntitiesByClass(org.bukkit.entity.Arrow.class)) {
					Location loc = arrow.getLocation();
					if (isInArea(loc, min, max)) {
						arrow.remove();
					}
				}

				getLogger().info("🏹 Smazány všechny šípy v aréně: " + arenaName);
			}
		}

	}

	private boolean isInArea(Location loc, Location min, Location max) {
		return loc.getX() >= min.getX() && loc.getX() <= max.getX()
				&& loc.getY() >= min.getY() && loc.getY() <= max.getY()
				&& loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ()
				&& loc.getWorld().equals(min.getWorld());
	}

	public void updateMainConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}


	public static NerKubKnockBackFFA getPlugin() {
		return plugin;
	}

	public CustomConfig getMessages() {
		return messages;
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

	public CustomConfig getMenu() {
		return menu;
	}

	public CustomConfig getEvents() {
		return events;
	}

	public CustomConfig getKits() {
		return kits;
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

	public BossBarManager getBossBarManager() {
		return bossBarManager;
	}

	public CustomEventManager getCustomEventManager() {
		return customEventManager;
	}

	public SafeZoneManager getSafeZoneManager() {
		return safeZoneManager;
	}


	public KitManager getKitManager() {
		return kitManager;
	}
}
