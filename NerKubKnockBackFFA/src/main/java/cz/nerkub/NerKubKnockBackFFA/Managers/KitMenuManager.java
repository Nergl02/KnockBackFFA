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

import java.util.List;

public class KitMenuManager implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DatabaseManager databaseManager;

	public KitMenuManager(NerKubKnockBackFFA plugin, DatabaseManager databaseManager) {
		this.plugin = plugin;
		this.databaseManager = databaseManager;
	}

	public void openKitMenu(Player player) {
		Inventory kitInventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&',
				"&6Select or Buy a Kit"));

		String selectedKit = databaseManager.getSelectedKit(player.getUniqueId()); // Získání aktuálně vybraného kitu

		for (String kitName : plugin.getKitManager().getKits()) {
			Material displayMaterial = Material.valueOf(plugin.getKitManager().getKitDisplayItem(kitName));
			int price = plugin.getKitManager().getKitPrice(kitName);
			boolean ownsKit = databaseManager.hasKit(player.getUniqueId(), kitName); // Kontrola vlastnictví kitu

			ItemStack kitItem = new ItemStack(displayMaterial);
			ItemMeta meta = kitItem.getItemMeta();

			if (meta != null) {
				meta.setDisplayName(ownsKit
						? ChatColor.GREEN + kitName // Zelená pro zakoupené kity
						: ChatColor.RED + kitName); // Červená pro nezakoupené

				if (ownsKit) {
					if (kitName.equals(selectedKit)) {
						meta.setLore(List.of(
								ChatColor.GOLD + "⭐ Selected Kit!", // Zlatá pro aktuálně vybraný kit
								ChatColor.YELLOW + "Click to select another!"
						));
					} else {
						meta.setLore(List.of(
								ChatColor.GREEN + "✅ You own this kit!",
								ChatColor.YELLOW + "Click to select!"
						));
					}
				} else {
					meta.setLore(List.of(
							ChatColor.RED + "❌ Price: " + price + " coins",
							ChatColor.YELLOW + "Click to buy!"
					));
				}

				kitItem.setItemMeta(meta);
			}

			kitInventory.addItem(kitItem);
		}

		player.openInventory(kitInventory);
	}

	@EventHandler
	public void onKitMenuClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&6Select or Buy a Kit")))
			return;

		event.setCancelled(true);

		ItemStack clickedItem = event.getCurrentItem();
		if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) return;

		String kitName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
		if (kitName == null || kitName.isEmpty()) return;

		int price = plugin.getKitManager().getKitPrice(kitName);
		int playerCoins = plugin.getPlayerStatsManager().getStats(player.getUniqueId()).getCoins();

		if (databaseManager.hasKit(player.getUniqueId(), kitName)) {
			databaseManager.setSelectedKit(player.getUniqueId(), kitName);
			player.sendMessage(ChatColor.GREEN + "✅ You selected the kit " + kitName + "!");
		} else {
			if (playerCoins >= price) {
				plugin.getPlayerStatsManager().getStats(player.getUniqueId()).setCoins(playerCoins - price);
				databaseManager.addKit(player.getUniqueId(), kitName);
				player.sendMessage(ChatColor.GREEN + "✅ Kit " + kitName + " purchased!");
			} else {
				player.sendMessage(ChatColor.RED + "❌ Not enough coins!");
			}
		}

		player.closeInventory(); // Zavření menu
	}

}
