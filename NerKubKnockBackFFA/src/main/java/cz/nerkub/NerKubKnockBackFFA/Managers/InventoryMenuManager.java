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

import java.util.Arrays;
import java.util.Objects;

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
		// Vytvoření inventáře s kapacitou 45 slotů
		Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")));

		// Načtení hlavního inventáře a hotbaru z databáze
		ItemStack[] mainInventory = databaseManager.loadMainInventory(player.getUniqueId());
		ItemStack[] hotbar = databaseManager.loadHotbar(player.getUniqueId());

		// Pokud není inventář uložen v databázi, použij výchozí hodnoty (např. prázdný inventář)
		if (mainInventory == null || Arrays.stream(mainInventory).allMatch(Objects::isNull)) {
			mainInventory = defaultInventoryManager.getDefaultMainInventory(); // Výchozí inventář
		}

		if (hotbar == null || Arrays.stream(hotbar).allMatch(Objects::isNull)) {
			hotbar = defaultInventoryManager.getDefaultHotbar(); // Výchozí hotbar
		}

		// 🔳 1. řádek - Hráčův hotbar (sloty 36–44)
		for (int i = 0; i < 9; i++) {
			inv.setItem(36 + i, hotbar[i] != null ? hotbar[i] : new ItemStack(Material.AIR));
		}

		// 🟦 2. řádek - Skleněná přepážka (sloty 27–35)
		ItemStack glass = createGlassPane(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.section-separator.display-name")));
		for (int i = 27; i < 36; i++) {
			inv.setItem(i, glass);
		}

		// 🟢 Uložit tlačítko (slot 30)
		inv.setItem(30, createButton(Material.valueOf(
				plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.material").toUpperCase()
		), ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.display-name"))));

		// 🔴 Resetovat tlačítko (slot 32)
		inv.setItem(32, createButton(Material.valueOf(
				plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.material").toUpperCase()
		), ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.display-name"))));

		// 🔲 3.–5. řádek - Zbytek inventáře (sloty 0–26)
		for (int i = 0; i < 27; i++) {
			inv.setItem(i, mainInventory[i] != null ? mainInventory[i] : new ItemStack(Material.AIR));
		}

		// 📜 Otevření inventáře hráči
		player.openInventory(inv);
	}


	// 💾 Uložení inventáře
	public void savePlayerInventory(Player player, Inventory inv) {
		ItemStack[] mainInventory = new ItemStack[27];
		ItemStack[] hotbar = new ItemStack[9];

		// 🔲 Uložení hlavního inventáře (sloty 0–26)
		for (int i = 0; i < 27; i++) {
			mainInventory[i] = inv.getItem(i);
		}

		// 🔳 Uložení hotbaru (sloty 36–44)
		for (int i = 0; i < 9; i++) {
			hotbar[i] = inv.getItem(36 + i);
		}

		// 📀 Uložení do databáze
		databaseManager.savePlayerInventory(player.getUniqueId(), mainInventory, hotbar);
	}


	// 🛑 Kliknutí v editoru
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")))) return;

		int slot = event.getRawSlot();
		ItemStack clicked = event.getCurrentItem();

		// Zakázat manipulaci se sklem a tlačítky, které nejsou určeny pro interakci (pro panely skla)
		if (slot >= 27 && slot <= 35 && (slot != 30 && slot != 32)) {
			event.setCancelled(true);  // Zablokování přetahování na skleněné panely
			return;
		}

		// Zakázat interakci s hráčovým skutečným inventářem (sloty 45 a výš)
		if (slot >= 45) {
			event.setCancelled(true);  // Zablokování interakce s hráčovým inventářem
			return;
		}

		// Pokud bylo kliknuto na nějaký item
		if (clicked != null && clicked.hasItemMeta()) {

			// Ověříme, zda je item skutečně emerald block nebo redstone block
			if (clicked.getType() == Material.valueOf(
					plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.save-inventory.material").toUpperCase())) {
				savePlayerInventory(player, event.getInventory()); // Uložení inventáře
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("inventory-editor-menu.save-inventory")));
				// Místo zavření inventáře ho necháme otevřený a pouze aktualizujeme položky
				updateInventory(player);
			} else if (clicked.getType() == Material.valueOf(
					plugin.getMenu().getConfig().getString("inventory-editor-menu.buttons.reset-inventory.material").toUpperCase())) {
				resetToDefault(player, event.getInventory());  // Reset na výchozí inventář
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("inventory-editor-menu.reset-inventory")));
				// Opět místo zavření jen znovu otevřeme editor a resetujeme inventář i tlačítka
				updateInventory(player);
			}
		}
	}


	// 🔄 Reset na výchozí inventář
	private void resetToDefault(Player player, Inventory inv) {
		ItemStack[] defaultInv = defaultInventoryManager.getDefaultMainInventory();
		ItemStack[] defaultHotbar = defaultInventoryManager.getDefaultHotbar();

		// Resetuj hlavní inventář (sloty 0–26)
		for (int i = 0; i < 27; i++) {
			inv.setItem(i, defaultInv[i] != null ? defaultInv[i] : new ItemStack(Material.AIR));
		}

		// Resetuj hotbar (sloty 36–44)
		for (int i = 0; i < 9; i++) {
			inv.setItem(36 + i, defaultHotbar[i] != null ? defaultHotbar[i] : new ItemStack(Material.AIR));
		}
		updateInventory(player);
	}

	private void updateInventory(Player player) {
		openInventoryEditor(player);
	}

	// 📏 Uložení při zavření
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("inventory-editor-menu.title")))) return;

		Player player = (Player) event.getPlayer();
		savePlayerInventory(player, event.getInventory());  // Změněno na savePlayerInventory
	}



	// 🌟 Vytvoření tlačítek
	private ItemStack createButton(Material material, String name) {
		ItemStack button = new ItemStack(material);
		ItemMeta meta = button.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			button.setItemMeta(meta);
		}
		return button;
	}

	// 🟦 Skleněné panely
	private ItemStack createGlassPane(String name) {
		ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		ItemMeta meta = pane.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			pane.setItemMeta(meta);
		}
		return pane;
	}
}
