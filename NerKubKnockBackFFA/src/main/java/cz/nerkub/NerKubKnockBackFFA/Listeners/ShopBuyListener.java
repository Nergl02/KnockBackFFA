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
			String levitationBootsName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("levitation-boots.display-name")));
			String swapperBallName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("swapper-ball.display-name")));
			String invisibilityCloakName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("invisibility-cloak.display-name")));
			String fireBallLauncherName= ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("fireball-launcher.display-name")));
			String explodingChickName= ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("exploding-chick.display-name")));
			String blazindDashName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("blazing-dash.display-name")));
			String speedBoostName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getItems().getConfig().getString("speed-boost.display-name")));

			if (itemName.equals(levitationBootsName)) {
				shopManager.purchaseItem(player, clickedItem, "levitation-boots");
			} else if (itemName.equals(swapperBallName)) {
				shopManager.purchaseItem(player, clickedItem, "swapper-ball");
			} else if (itemName.equals(invisibilityCloakName)) {
				shopManager.purchaseItem(player, clickedItem, "invisibility-cloak");
			} else if (itemName.equals(fireBallLauncherName)) {
				shopManager.purchaseItem(player, clickedItem, "fireball-launcher");
			} else if (itemName.equals(explodingChickName)) {
				shopManager.purchaseItem(player, clickedItem, "exploding-chick");
			} else if (itemName.equals(blazindDashName)) {
				shopManager.purchaseItem(player, clickedItem, "blazing-dash");
			} else if (itemName.equals(speedBoostName)) {
				shopManager.purchaseItem(player, clickedItem, "speed-boost");
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
						plugin.getMessages().getConfig().getString("unknown-item")));
			}
		}
	}
}