package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class InventoryMenuManager implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DatabaseManager databaseManager;
	private final DefaultInventoryManager defaultInventoryManager;

	public InventoryMenuManager(NerKubKnockBackFFA plugin, DatabaseManager databaseManager, DefaultInventoryManager defaultInventoryManager) {
		this.plugin = plugin;
		this.databaseManager = databaseManager;
		this.defaultInventoryManager = defaultInventoryManager;
	}

	public void openInventoryEditor(Player player) {
		String selectedKit = databaseManager.getSelectedKit(player.getUniqueId());
		boolean hasKit = selectedKit != null;

		Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")));

		ItemStack[] mainInventory;
		ItemStack[] hotbar;
		ItemStack[] armor = new ItemStack[4];

		if (hasKit) {
			if (databaseManager.hasCustomKit(player.getUniqueId(), selectedKit)) {
				mainInventory = databaseManager.loadCustomKit(player.getUniqueId(), selectedKit, false);
				hotbar = databaseManager.loadCustomKit(player.getUniqueId(), selectedKit, true);
				armor = databaseManager.loadCustomKitArmor(player.getUniqueId(), selectedKit);
			} else {
				mainInventory = plugin.getKitManager().getKitItems(selectedKit);
				hotbar = defaultInventoryManager.getDefaultHotbar();
				armor = plugin.getKitManager().getKitArmor(selectedKit);
			}
		} else {
			// ✅ Pokud hráč nemá žádný kit, načte se defaultní inventář i brnění
			player.sendMessage(ChatColor.YELLOW + "⚠ Nemáš aktivní žádný kit, zobrazujeme výchozí výbavu.");
			mainInventory = defaultInventoryManager.getDefaultMainInventory();
			hotbar = defaultInventoryManager.getDefaultHotbar();
			armor = new ItemStack[4]; // Žádné brnění
		}

		boolean hasArrow = false;
		for (ItemStack item : mainInventory) {
			if (item != null && item.getType() == Material.ARROW) {
				hasArrow = true;
				break;
			}
		}

		if (!hasArrow) {
			// Přidání šípu **před** nahráním kitu
			for (int i = 0; i < mainInventory.length; i++) {
				if (mainInventory[i] == null || mainInventory[i].getType() == Material.AIR) {
					mainInventory[i] = new ItemStack(Material.ARROW, 1);
					break; // Přidáme **jen jeden šíp**
				}
			}
		}

		for (int i = 0; i < 27; i++) {
			inv.setItem(i, mainInventory[i] != null ? mainInventory[i] : new ItemStack(Material.AIR));
		}

		ItemStack glass = createGlassPane(" ");
		for (int i = 27; i < 36; i++) {
			inv.setItem(i, glass);
		}

		inv.setItem(30, createButton(Material.valueOf(plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.material")), ChatColor.translateAlternateColorCodes('&', plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.display-name"))));
		inv.setItem(32, createButton(Material.valueOf(plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.material")), ChatColor.translateAlternateColorCodes('&', plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.display-name"))));

		for (int i = 0; i < 9; i++) {
			inv.setItem(36 + i, hotbar[i] != null ? hotbar[i] : new ItemStack(Material.AIR));
		}

		player.openInventory(inv);
		if (!plugin.getSafeZoneManager().isInSafeZone(player.getLocation(), plugin.getArenaManager().getArenaSpawn(plugin.getArenaManager().getCurrentArenaName()))) {
			player.getInventory().setArmorContents(armor);
		}
	}

	public void savePlayerKit(Player player, Inventory inv) {
		String selectedKit = databaseManager.getSelectedKit(player.getUniqueId());

		if (selectedKit == null) {
			player.sendMessage(ChatColor.RED + "❌ Nemáš aktivní žádný kit!");
			return;
		}

		ItemStack[] mainInventory = new ItemStack[27];
		ItemStack[] hotbar = new ItemStack[9];

		for (int i = 0; i < 27; i++) {
			mainInventory[i] = inv.getItem(i);
		}

		for (int i = 0; i < 9; i++) {
			hotbar[i] = inv.getItem(36 + i);
		}

		ItemStack[] armor = null;
		if (!plugin.getSafeZoneManager().isInSafeZone(player.getLocation(), plugin.getArenaManager().getArenaSpawn(plugin.getArenaManager().getCurrentArenaName()))) {
			armor = databaseManager.loadCustomKitArmor(player.getUniqueId(), selectedKit);
		}


		databaseManager.saveCustomKit(player.getUniqueId(), selectedKit, mainInventory, hotbar, armor);

		player.sendMessage(ChatColor.GREEN + "✅ Tvůj kit '" + selectedKit + "' byl uložen!");
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")))) return;

		int slot = event.getRawSlot();
		ItemStack clicked = event.getCurrentItem();
		String selectedKit = databaseManager.getSelectedKit(player.getUniqueId());

		if (slot == 30) {
			event.setCancelled(true);
			savePlayerKit(player, event.getInventory()); // ✅ Uložení kitu
			player.sendMessage(ChatColor.GREEN + "✅ Tvůj kit byl uložen!");
		} else if (slot == 32) {
			event.setCancelled(true);
			resetToDefault(player, event.getInventory(), selectedKit); // ✅ Reset kitu (nebo defaultního inventáře)
			player.sendMessage(ChatColor.YELLOW + "⚠️ Inventář byl resetován na výchozí hodnoty.");
		}

		if (slot >= 27 && slot <= 35) {
			event.setCancelled(true);
			return;
		}

		if (slot >= 45) {
			event.setCancelled(true);
			return;
		}

		if (clicked != null && clicked.hasItemMeta()) {
			if (clicked.getType() == Material.valueOf(plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.material"))) {
				savePlayerKit(player, event.getInventory());
			} else if (clicked.getType() == Material.valueOf(plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.material"))) {
				resetToDefault(player, event.getInventory(), selectedKit);
				player.sendMessage(ChatColor.YELLOW + "⚠️ Kit byl resetován na výchozí hodnoty.");
			}
		}
	}

	private void resetToDefault(Player player, Inventory inv, String kitName) {
		ItemStack[] mainInventory = new ItemStack[27];
		ItemStack[] defaultInv;
		ItemStack[] defaultHotbar;
		ItemStack[] defaultArmor;

		boolean hasArrow = false;
		for (ItemStack item : mainInventory) {
			if (item != null && item.getType() == Material.ARROW) {
				hasArrow = true;
				break;
			}
		}

		if (!hasArrow) {
			// Přidání šípu **před** nahráním kitu
			for (int i = 0; i < mainInventory.length; i++) {
				if (mainInventory[i] == null || mainInventory[i].getType() == Material.AIR) {
					mainInventory[i] = new ItemStack(Material.ARROW, 1);
					break; // Přidáme **jen jeden šíp**
				}
			}
		}

		if (kitName != null) {
			// ✅ Hráč má aktivní kit → načteme jeho výchozí hodnoty
			defaultInv = plugin.getKitManager().getKitItems(kitName);
			defaultHotbar = defaultInventoryManager.getDefaultHotbar();
			defaultArmor = plugin.getKitManager().getKitArmor(kitName);
		} else {
			// ❌ Hráč nemá aktivní kit → načteme defaultní inventář
			defaultInv = defaultInventoryManager.getDefaultMainInventory();
			defaultHotbar = defaultInventoryManager.getDefaultHotbar();
			defaultArmor = new ItemStack[4]; // Žádné brnění
		}

		for (int i = 0; i < 27; i++) {
			inv.setItem(i, defaultInv[i] != null ? defaultInv[i] : new ItemStack(Material.AIR));
		}

		for (int i = 0; i < 9; i++) {
			inv.setItem(36 + i, defaultHotbar[i] != null ? defaultHotbar[i] : new ItemStack(Material.AIR));
		}

		player.getInventory().setArmorContents(defaultArmor);
	}


	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")))) return;

		Player player = (Player) event.getPlayer();
		savePlayerKit(player, event.getInventory());
	}

	private ItemStack createButton(Material material, String name) {
		ItemStack button = new ItemStack(material);
		ItemMeta meta = button.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			button.setItemMeta(meta);
		}
		return button;
	}

	private ItemStack createGlassPane(String name) {
		ItemStack pane = new ItemStack(Material.valueOf(plugin.getMenu().getConfig().getString("inventory-editor-menu.section-separator.material")));
		ItemMeta meta = pane.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			pane.setItemMeta(meta);
		}
		return pane;
	}
}
