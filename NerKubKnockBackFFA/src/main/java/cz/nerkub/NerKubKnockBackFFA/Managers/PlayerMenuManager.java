package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerMenuManager implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final InventoryMenuManager inventoryMenuManager;

	public PlayerMenuManager(NerKubKnockBackFFA plugin, InventoryMenuManager inventoryMenuManager) {
		this.plugin = plugin;
		this.inventoryMenuManager = inventoryMenuManager;
	}

	public void openMenu(Player player) {
		Inventory menuInventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("main-menu.title")));

		// 🛠️ 1. Tlačítko pro nastavení inventáře
		ItemStack inventoryButton = new ItemStack(Material.valueOf(
				plugin.getMenu().getConfig().getString("main-menu.buttons.inventory-editor.material").toUpperCase()));
		ItemMeta invMeta = inventoryButton.getItemMeta();
		invMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("main-menu.buttons.inventory-editor.display-name")));
		inventoryButton.setItemMeta(invMeta);
		menuInventory.setItem(11, inventoryButton);

		// 🏆 2. Tlačítko pro statistiky
		ItemStack statsButton = new ItemStack(Material.valueOf(
				plugin.getMenu().getConfig().getString("main-menu.buttons.stats.material").toUpperCase()));
		ItemMeta statsMeta = statsButton.getItemMeta();
		statsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("main-menu.buttons.stats.display-name")));
		statsButton.setItemMeta(statsMeta);
		menuInventory.setItem(15, statsButton);

		// 🏠 4. Výplň
		ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta fillerMeta = filler.getItemMeta();
		fillerMeta.setDisplayName(" ");
		filler.setItemMeta(fillerMeta);
		for (int i = 0; i < 27; i++) {
			if (menuInventory.getItem(i) == null) {
				menuInventory.setItem(i, filler);
			}
		}

		// Otevření menu
		player.openInventory(menuInventory);
	}

	// 📌 Event pro kliknutí v menu
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("main-menu.title")))) {
			event.setCancelled(true);  // Neumožnit přetahování itemů

			ItemStack clickedItem = event.getCurrentItem();
			if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

			Material invEditMaterial = Material.valueOf(plugin.getMenu().getConfig().getString("main-menu.buttons.inventory-editor.material").toUpperCase());
			Material statsMaterial = Material.valueOf(plugin.getMenu().getConfig().getString("main-menu.buttons.stats.material").toUpperCase());

			if (clickedItem.getType() == invEditMaterial) {
				player.closeInventory();
				inventoryMenuManager.openInventoryEditor(player);  // Otevření menu pro inventář
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("inventory-editor-menu.inventory-opened")));
			} else if (clickedItem.getType() == statsMaterial) {
				player.sendMessage(ChatColor.AQUA + "SOON");
			}

		}
	}
}
