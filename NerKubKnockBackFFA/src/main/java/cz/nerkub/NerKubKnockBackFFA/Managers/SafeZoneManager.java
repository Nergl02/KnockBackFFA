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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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
	private final Map<Player, ItemStack[]> storedInventories = new HashMap<>();
	private final Map<Player, ItemStack[]> storedArmor = new HashMap<>();
	private final Map<UUID, List<Class<? extends Event>>> pendingEvents = new HashMap<>();
	private final Set<UUID> playersInSafeZone = new HashSet<>();

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

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String currentArena = arenaManager.getCurrentArenaName();
		Location arenaSpawn = arenaManager.getArenaSpawn(currentArena);

		boolean inSafeZone = isInSafeZone(player.getLocation(), arenaSpawn);

		if (inSafeZone) {
			if (!storedInventories.containsKey(player)) {
				enterSafeZone(player);
			}
		} else {
			if (storedInventories.containsKey(player)) {
				exitSafeZone(player);
			}

			// 🎯 **Zabránění duplicitnímu připojení k eventu**
			if (!playersInSafeZone.contains(playerId)) return;
			playersInSafeZone.remove(playerId); // ✅ Označení hráče jako "mimo safezónu"

			// ✅ **Přidej hráče do aktivních eventů, ale jen pokud tam ještě není**
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

		// ✅ Otevření menu podle itemu
		if (item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&6Main Menu"))) {
			playerMenuManager.openMenu(player);
			event.setCancelled(true);
		} else if (item.getType() == Material.EMERALD && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&aShop"))) {
			shopManager.openShop(player);
			event.setCancelled(true);
		} else if (item.getType() == Material.CHEST && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&bKits"))) {
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
		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius"); // Musí odpovídat radiusu, který jsi definoval
		return location.distance(arenaSpawn) <= safeZoneRadius;
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


	private void enterSafeZone(Player player) {
		// 🌟 Uložit inventář před vstupem do safezóny
		storedInventories.put(player, player.getInventory().getContents().clone());
		storedArmor.put(player, player.getInventory().getArmorContents().clone());

		playersInSafeZone.add(player.getUniqueId());
		// 🧹 Vyčistit inventář
		player.getInventory().clear();

		// 🛡 Uložit pouze obsah inventáře, ale NE brnění!
		player.getInventory().setItem(0, createMenuItem(Material.NETHER_STAR, "&6Main Menu"));
		player.getInventory().setItem(1, createMenuItem(Material.EMERALD, "&aShop"));
		player.getInventory().setItem(2, createMenuItem(Material.CHEST, "&bKits"));

		List<Class<? extends Event>> activeEvents = new ArrayList<>();
		if (plugin.getCustomEventManager().isEventActive(String.valueOf(LowGravityEvent.class))) {
			activeEvents.add(LowGravityEvent.class);
		}
		if (plugin.getCustomEventManager().isEventActive(String.valueOf(ExtraPunchBowEvent.class))) {
			activeEvents.add(ExtraPunchBowEvent.class);
		}
		if (plugin.getCustomEventManager().isEventActive(String.valueOf(NoKnockBackStickEvent.class))) {
			activeEvents.add(NoKnockBackStickEvent.class);
		}
		pendingEvents.put(player.getUniqueId(), activeEvents);

		player.sendMessage(ChatColor.GREEN + "🏰 You entered the safezone!");
	}

	private void exitSafeZone(Player player) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		UUID playerId = player.getUniqueId();

		// 🌟 Obnovení inventáře (pokud hráč NEMÁ kit)
		player.getInventory().setContents(storedInventories.get(player));
		player.getInventory().setArmorContents(storedArmor.get(player));
		storedInventories.remove(player);
		storedArmor.remove(player);

		// 🎁 Předání zakoupených itemů po opuštění safezóny
		if (pendingItems.containsKey(playerId)) {
			List<ItemStack> items = pendingItems.remove(playerId);
			if (items != null && !items.isEmpty()) {
				for (ItemStack item : items) {
					player.getInventory().addItem(item);
				}
				player.sendMessage(ChatColor.GOLD + "🎁 Obdržel jsi zakoupené itemy!");
			}
		}

		// 🌟 Obnovení brnění, pokud bylo uloženo v safezóně
		if (storedArmor.containsKey(player) && storedArmor.get(player) != null) {
			player.getInventory().setArmorContents(storedArmor.get(player));
		} else {
			player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null}); // Prázdné brnění
		}

		// 🎭 Aktivace kitu, pokud hráč nějaký vybral
		String selectedKit = databaseManager.getSelectedKit(playerId);
		if (selectedKit != null) {
			player.getInventory().clear();
			if (plugin.getDatabaseManager().hasCustomKit(playerId, selectedKit)) {
				ItemStack[] savedMainInv = plugin.getDatabaseManager().loadCustomKit(playerId, selectedKit, false);
				ItemStack[] savedHotbar = plugin.getDatabaseManager().loadCustomKit(playerId, selectedKit, true);
				ItemStack[] savedArmor = plugin.getDatabaseManager().loadCustomKitArmor(playerId, selectedKit);


				// ✅ Aplikujeme obsah hotbaru
				for (int i = 0; i < 9; i++) {
					if (savedHotbar[i] != null) {
						player.getInventory().setItem(i, savedHotbar[i]);
						if (plugin.getConfig().getBoolean("debug")) {
							Bukkit.getLogger().info("Hotbar slot load " + i + ": " + getItemName(savedHotbar[i]));
						}
					}
				}

				for (int i = 0; i < 27; i++) {
					if (savedMainInv[i] != null) {
						player.getInventory().setItem(i + 9, savedMainInv[i]); // ✔ Teď se to zapíše od slotu 9
						if (plugin.getConfig().getBoolean("debug")) {
							Bukkit.getLogger().info("MainInventory slot upload " + i + ": " + getItemName(savedMainInv[i]));
						}
					}
				}

				// Kontrola, zda hráč má šíp v inventáři, pokud ne, přidáme ho
				boolean hasArrow = false;
				for (ItemStack item : player.getInventory().getContents()) {
					if (item != null && item.getType() == Material.ARROW) {
						hasArrow = true;
						break;
					}
				}

// Pokud hráč nemá šíp, přidáme mu ho
				if (!hasArrow) {
					player.getInventory().addItem(new ItemStack(Material.ARROW));
					if (plugin.getConfig().getBoolean("debug")) {
						Bukkit.getLogger().info("Arrow added to player inventory.");
					}
				}

				if (!isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
					if (savedArmor != null) {
						player.getInventory().setArmorContents(savedArmor);
						if (plugin.getConfig().getBoolean("debug")) {
							player.sendMessage(ChatColor.GREEN + "✔ Brnění bylo načteno pro kit " + selectedKit);
						}
					} else {
						if (plugin.getConfig().getBoolean("debug")) {
							player.sendMessage(ChatColor.RED + "⚠ Kit " + selectedKit + " nemá žádné brnění.");
						}
					}
				} else {
					if (plugin.getConfig().getBoolean("debug")) {
						player.sendMessage(ChatColor.YELLOW + "⚠ Jsi v safezóně, brnění se nenasadilo.");
					}
				}

				player.getInventory().setArmorContents(savedArmor);
				if (plugin.getConfig().getBoolean("debug")) {
					player.sendMessage(ChatColor.GREEN + "✔ Načtena tvoje uložená verze kitu: " + selectedKit);
				}
			} else {
				// 🔹 Hráč NEMÁ custom verzi kitu – dostane výchozí kit a přidáme defaultní hotbar i main inventory
				kitManager.applyKit(player, selectedKit);
				ItemStack[] defaultHotbar = defaultInventoryManager.getDefaultHotbar();
				ItemStack[] defaultMainInventory = defaultInventoryManager.getDefaultMainInventory();

				// ✅ Doplnění hotbaru, pokud jsou tam prázdná místa
				for (int i = 0; i < 9; i++) {
					if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
						player.getInventory().setItem(i, defaultHotbar[i]);
					}
				}

				for (int i = 0; i < defaultMainInventory.length; i++) {
					if (defaultMainInventory[i] != null) {
						player.getInventory().addItem(defaultMainInventory[i]);
					}
				}
			}
		} else {
			defaultInventoryManager.setDefaultInventory(player);
			if (plugin.getConfig().getBoolean("debug")) {
				player.sendMessage(ChatColor.GRAY + "⚔ Nemáš žádný kit, dostal jsi základní výbavu.");
			}
		}

		// ✅ **Pokud má hráč naplánované vrácení KnockBack Sticku, vrátíme ho teď!**
		if (plugin.getCustomEventManager().shouldReturnKnockBackStick(playerId)) {
			player.getInventory().addItem(knockBackStickItem.createKnockBackStickItem());
			plugin.getCustomEventManager().removeKnockBackStickReturn(playerId);
		}

		// ✅ Reset zpracování hráče pro Extra Punch Bow (aby ho dostal znovu)
		plugin.getCustomEventManager().resetProcessedPlayer(playerId);

		// ✅ Pokud hráč má naplánované vrácení Extra Punch Bow, dostane ho teď!
		if (plugin.getCustomEventManager().shouldReturnExtraPunchBow(playerId)) {
			plugin.getCustomEventManager().restoreExtraPunchBow(player);
		}

		// 🔥 **Aplikujeme efekty aktuálně probíhajícího eventu, pokud existuje**
		Event currentEvent = plugin.getCustomEventManager().getCurrentEvent();
		if (currentEvent != null) {
			if (currentEvent instanceof LowGravityEvent) {
				((LowGravityEvent) currentEvent).applyGravityEffect(player);
			} else if (currentEvent instanceof ExtraPunchBowEvent) {
				((ExtraPunchBowEvent) currentEvent).giveExtraPunchBow(player);
			} else if (currentEvent instanceof NoKnockBackStickEvent) {
				((NoKnockBackStickEvent) currentEvent).removeKnockBackStick(player);
			}
		}

		storedInventories.remove(player);
		storedArmor.remove(player);
		player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
				plugin.getMessages().getConfig().getString("arena.safe-zone-leave")));

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


	private ItemStack createMenuItem(Material material, String name) {
		ItemStack item = new ItemStack(material);
		var meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
			item.setItemMeta(meta);
		}
		return item;
	}
}
