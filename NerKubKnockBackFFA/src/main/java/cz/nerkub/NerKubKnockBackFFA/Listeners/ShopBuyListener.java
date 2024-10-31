package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.Managers.ShopManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopBuyListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final ShopManager shopManager;

	public ShopBuyListener(NerKubKnockBackFFA plugin, ShopManager shopManager) {
		this.plugin = plugin;
		this.shopManager = shopManager;
	}

	@EventHandler
	public void onShopClick(InventoryClickEvent event) {
		// Zkontrolujeme, zda je kliknutí v inventáři shopu
		Player player = (Player) event.getWhoClicked();
		Inventory clickedInventory = event.getClickedInventory();

		if (clickedInventory == null || event.getView().getTitle() == null) {
			return;
		}

		// Kontrolujeme, zda je inventář s názvem shopu
		String shopTitle = ChatColor.translateAlternateColorCodes('&', plugin.getShop().getConfig().getString("title"));
		if (event.getView().getTitle().equals(shopTitle)) {
			event.setCancelled(true); // Zamezíme výchozímu chování při kliknutí na item v shopu

			ItemStack clickedItem = event.getCurrentItem();
			if (clickedItem == null || clickedItem.getType() == Material.AIR) {
				return;
			}

			// Získáme jméno itemu a ověříme, zda odpovídá některému z dostupných předmětů k nákupu
			ItemMeta itemMeta = clickedItem.getItemMeta();
			if (itemMeta == null || !itemMeta.hasDisplayName()) {
				return;
			}

			String itemName = ChatColor.stripColor(itemMeta.getDisplayName());
			switch (itemName) {
				case "Levitation Boots":
					shopManager.purchaseItem(player, clickedItem, "levitation-boots");
					break;

				case "Swapper Ball":
					shopManager.purchaseItem(player, clickedItem, "swapper-ball");
					break;

				default:
					player.sendMessage(ChatColor.RED + "Unknown item.");
			}
		}
	}
}