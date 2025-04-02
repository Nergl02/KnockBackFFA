package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Events.ExtraPunchBowEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.LowGravityEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.NoKnockBackStickEvent;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SafeZoneManager implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;
	private final PlayerMenuManager playerMenuManager;
	private final ShopManager shopManager;
	private final KitManager kitManager;
	private final KitMenuManager kitMenuManager;
	private final DatabaseManager databaseManager;
	private final DefaultInventoryManager defaultInventoryManager;
	private final LeatherTunicItem leatherTunicItem;
	private final KnockBackStickItem knockBackStickItem;

	private final Map<UUID, List<ItemStack>> pendingItems = new HashMap<>();
	private final Map<UUID, ItemStack[]> storedInventories = new HashMap<>();
	private final Map<UUID, ItemStack[]> storedArmor = new HashMap<>();
	private final Map<UUID, List<Class<? extends Event>>> pendingEvents = new HashMap<>();
	private final Set<UUID> playersInSafeZone = new HashSet<>();

	public String cachedArenaName = null;
	public Location cachedArenaSpawn = null;


	public SafeZoneManager(NerKubKnockBackFFA plugin, ArenaManager arenaManager, PlayerMenuManager playerMenuManager, ShopManager shopManager, KitManager kitManager, KitMenuManager kitMenuManager, DatabaseManager databaseManager, DefaultInventoryManager defaultInventoryManager, LeatherTunicItem leatherTunicItem, KnockBackStickItem knockBackStickItem) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.playerMenuManager = playerMenuManager;
		this.shopManager = shopManager;
		this.kitManager = kitManager;
		this.kitMenuManager = kitMenuManager;
		this.databaseManager = databaseManager;
		this.defaultInventoryManager = defaultInventoryManager;
		this.leatherTunicItem = leatherTunicItem;
		this.knockBackStickItem = knockBackStickItem;
	}

	public void updateActiveArena(String arenaName) {
		this.cachedArenaName = arenaName;
		this.cachedArenaSpawn = arenaManager.getArenaSpawn(arenaName);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getBlockX() == to.getBlockX() &&
				from.getBlockY() == to.getBlockY() &&
				from.getBlockZ() == to.getBlockZ()) {
			return;
		}

		if (cachedArenaSpawn == null) {
			if (plugin.getConfig().getBoolean("debug")) {
				Bukkit.getLogger().info("[DEBUG] cachedArenaSpawn je null, žádná aréna není nastavena.");
			}
			return;
		}

		boolean inSafeZone = isInSafeZone(to, cachedArenaSpawn);
		if (plugin.getConfig().getBoolean("debug")) {
			Bukkit.getLogger().info("[DEBUG] " + player.getName() + " safeZone = " + inSafeZone);
		}

		if (inSafeZone) {
			if (!storedInventories.containsKey(playerId)) {
				if (plugin.getConfig().getBoolean("debug")) {
					Bukkit.getLogger().info("[DEBUG] ➕ enterSafeZone() spuštěno pro " + player.getName());
				}
				enterSafeZone(player);
			} else {
				if (plugin.getConfig().getBoolean("debug")) {
					Bukkit.getLogger().info("[DEBUG] " + player.getName() + " už je v safezóně.");
				}
			}
		} else {
			if (storedInventories.containsKey(playerId)) {
				if (plugin.getConfig().getBoolean("debug")) {
					Bukkit.getLogger().info("[DEBUG] ➖ exitSafeZone() spuštěno pro " + player.getName());
				}
				exitSafeZone(player);
			}

			if (!playersInSafeZone.contains(playerId)) return;
			playersInSafeZone.remove(playerId);

			for (String eventName : plugin.getCustomEventManager().eventList) {
				if (plugin.getCustomEventManager().isEventActive(eventName)) {
					plugin.getCustomEventManager().applyEventEffect(player, eventName);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		if (item == null) return;

		if (isMatchingConfigItem(item, "main-menu-item")) {
			playerMenuManager.openMenu(player);
			event.setCancelled(true);
		} else if (isMatchingConfigItem(item, "shop-item")) {
			shopManager.openShop(player);
			event.setCancelled(true);
		} else if (isMatchingConfigItem(item, "kits-item")) {
			kitMenuManager.openKitMenu(player);
			event.setCancelled(true);
		}
	}


	public void buyItem(Player player, ItemStack item) {
		UUID playerId = player.getUniqueId();

		// Přidáme item do dočasného seznamu
		pendingItems.computeIfAbsent(playerId, k -> new ArrayList<>()).add(item);

		// Oznámení hráči
		player.sendMessage(ChatColor.YELLOW + "✔ Item zakoupen! Dostaneš ho, až opustíš safezónu.");
	}

	public boolean wasInSafeZone(UUID playerId) {
		return playersInSafeZone.contains(playerId);
	}

	public void setOutOfSafeZone(UUID playerId) {
		playersInSafeZone.remove(playerId);
	}

	public boolean isInSafeZone(Location location, Location arenaSpawn) {
		if (!location.getWorld().equals(arenaSpawn.getWorld())) return false;

		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius");
		return location.distanceSquared(arenaSpawn) <= safeZoneRadius * safeZoneRadius;
	}

	public boolean isLocationInSafeZone(Location location) {
		String currentArena = plugin.getArenaManager().getCurrentArenaName();
		Location arenaSpawn = plugin.getArenaManager().getArenaSpawn(currentArena);

		if (arenaSpawn == null) {
			return false; // Pokud není definovaný spawn, safezona neexistuje
		}

		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius");
		return location.distance(arenaSpawn) <= safeZoneRadius;
	}

	private boolean isMatchingConfigItem(ItemStack clicked, String path) {
		FileConfiguration config = plugin.getItems().getConfig();

		if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return false;

		Material expectedMaterial = Material.valueOf(config.getString(path + ".material"));
		String expectedName = ChatColor.translateAlternateColorCodes('&', config.getString(path + ".display-name"));

		return clicked.getType() == expectedMaterial &&
				clicked.getItemMeta().hasDisplayName() &&
				clicked.getItemMeta().getDisplayName().equals(expectedName);
	}

	private void enterSafeZone(Player player) {
		UUID playerId = player.getUniqueId();

		// ✅ Už je hráč v safezóně? Vynech akci
		if (storedInventories.containsKey(playerId)) return;

		storedInventories.put(playerId, player.getInventory().getContents().clone());
		storedArmor.put(playerId, player.getInventory().getArmorContents().clone());
		playersInSafeZone.add(playerId);
		player.getInventory().clear();

		// ✅ Cache config hodnoty na proměnné – levnější a přehlednější
		FileConfiguration config = plugin.getItems().getConfig();

		setMenuItem(player, config, "main-menu-item");
		setMenuItem(player, config, "shop-item");
		setMenuItem(player, config, "kits-item");

		// 📦 Zaznamenání aktivních eventů
		List<Class<? extends Event>> activeEvents = new ArrayList<>();
		if (plugin.getCustomEventManager().isEventActive(LowGravityEvent.class.getSimpleName())) {
			activeEvents.add(LowGravityEvent.class);
		}
		if (plugin.getCustomEventManager().isEventActive(ExtraPunchBowEvent.class.getSimpleName())) {
			activeEvents.add(ExtraPunchBowEvent.class);
		}
		if (plugin.getCustomEventManager().isEventActive(NoKnockBackStickEvent.class.getSimpleName())) {
			activeEvents.add(NoKnockBackStickEvent.class);
		}
		pendingEvents.put(playerId, activeEvents);

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("arena.safe-zone-enter")));
	}

	// 🛠 Pomocná metoda
	private void setMenuItem(Player player, FileConfiguration config, String path) {
		player.getInventory().setItem(
				config.getInt(path + ".slot"),
				createMenuItem(
						Material.valueOf(config.getString(path + ".material")),
						ChatColor.translateAlternateColorCodes('&', config.getString(path + ".display-name")),
						config.getStringList(path + ".lore")
				)
		);
	}

	private void exitSafeZone(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		UUID playerId = player.getUniqueId();

		// ✅ Obnova inventáře a brnění
		ItemStack[] inventory = storedInventories.remove(playerId);
		ItemStack[] armor = storedArmor.remove(playerId);

		if (inventory != null) player.getInventory().setContents(inventory);
		if (armor != null) player.getInventory().setArmorContents(armor);
		else player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});

		// 🎁 Vrácení pending itemů
		List<ItemStack> items = pendingItems.remove(playerId);
		if (items != null && !items.isEmpty()) {
			for (ItemStack item : items) {
				player.getInventory().addItem(item);
			}
			player.sendMessage(ChatColor.GOLD + "🎁 Obdržel jsi zakoupené itemy!");
		}

		// 🎯 Aktivace kitu
		String selectedKit = databaseManager.getSelectedKit(playerId);
		if (selectedKit != null) {
			player.getInventory().clear();

			if (plugin.getDatabaseManager().hasCustomKit(playerId, selectedKit)) {
				ItemStack[] savedMainInv = plugin.getDatabaseManager().loadCustomKit(playerId, selectedKit, false);
				ItemStack[] savedHotbar = plugin.getDatabaseManager().loadCustomKit(playerId, selectedKit, true);
				ItemStack[] savedArmor = plugin.getDatabaseManager().loadCustomKitArmor(playerId, selectedKit);

				// 💡 Hotbar: sloty 0–8
				for (int i = 0; i < 9; i++) {
					if (savedHotbar[i] != null) player.getInventory().setItem(i, savedHotbar[i]);
				}

				// 💡 Main inv: sloty 9–35
				for (int i = 0; i < 27; i++) {
					if (savedMainInv[i] != null) player.getInventory().setItem(i + 9, savedMainInv[i]);
				}

				// 🏹 Zkontroluj šíp
				boolean hasArrow = Arrays.stream(player.getInventory().getContents())
						.anyMatch(item -> item != null && item.getType() == Material.ARROW);

				if (!hasArrow) player.getInventory().addItem(new ItemStack(Material.ARROW));

				// 👕 Brnění pokud hráč není v safezóně
				if (!isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))
						&& savedArmor != null) {
					player.getInventory().setArmorContents(savedArmor);
				}

				player.sendMessage(ChatColor.GREEN + "✔ Načtena tvoje uložená verze kitu: " + selectedKit);
			} else {
				// ⚔️ Defaultní kit
				kitManager.applyKit(player, selectedKit);
				ItemStack[] hotbar = defaultInventoryManager.getDefaultHotbar();
				ItemStack[] mainInv = defaultInventoryManager.getDefaultMainInventory();

				for (int i = 0; i < 9; i++) {
					if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
						player.getInventory().setItem(i, hotbar[i]);
					}
				}
				for (ItemStack item : mainInv) {
					if (item != null) player.getInventory().addItem(item);
				}
			}
		} else {
			// ⚔️ Základní výbava
			defaultInventoryManager.setDefaultInventory(player);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("kits.no-active-kit")));
		}

		// 🥊 KnockBack Stick?
		if (plugin.getCustomEventManager().shouldReturnKnockBackStick(playerId)) {
			player.getInventory().addItem(knockBackStickItem.createKnockBackStickItem());
			plugin.getCustomEventManager().removeKnockBackStickReturn(playerId);
		}

		// 🎯 Extra Punch Bow
		plugin.getCustomEventManager().resetProcessedPlayer(playerId);
		if (plugin.getCustomEventManager().shouldReturnExtraPunchBow(playerId)) {
			plugin.getCustomEventManager().restoreExtraPunchBow(player);
		}

		// 🧪 Event efekty
		Event currentEvent = plugin.getCustomEventManager().getCurrentEvent();
		if (currentEvent instanceof LowGravityEvent) {
			((LowGravityEvent) currentEvent).applyGravityEffect(player);
		} else if (currentEvent instanceof ExtraPunchBowEvent) {
			((ExtraPunchBowEvent) currentEvent).giveExtraPunchBow(player);
		} else if (currentEvent instanceof NoKnockBackStickEvent) {
			((NoKnockBackStickEvent) currentEvent).removeKnockBackStick(player);
		}

		// 👕 Always nasadí tuniku
		player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());

		// 📢 Info zpráva
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				prefix + plugin.getMessages().getConfig().getString("arena.safe-zone-leave")));
	}

	private String getItemName(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return "EMPTY";
		}
		return item.getType().name() + " x" + item.getAmount();
	}

	private boolean hasArmor(ItemStack[] armor) {
		for (ItemStack item : armor) {
			if (item != null && item.getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}

	private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(displayName);
			if (lore != null && !lore.isEmpty()) {
				meta.setLore(lore.stream()
						.map(line -> ChatColor.translateAlternateColorCodes('&', line))
						.collect(Collectors.toList()));
			}
			item.setItemMeta(meta);
		}
		return item;
	}

}
