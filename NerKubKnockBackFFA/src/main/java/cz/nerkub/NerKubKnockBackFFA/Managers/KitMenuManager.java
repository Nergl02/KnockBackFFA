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
				plugin.getMenu().getConfig().getString("kits-menu.title")));

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
								ChatColor.translateAlternateColorCodes('&',
										plugin.getMessages().getConfig().getString("kits.selected")), // Zlatá pro aktuálně vybraný kit
								ChatColor.translateAlternateColorCodes('&',
										plugin.getMessages().getConfig().getString("kits.select-another"))
						));
					} else {
						meta.setLore(List.of(
								ChatColor.translateAlternateColorCodes('&',
										plugin.getMessages().getConfig().getString("kits.own")),
								ChatColor.translateAlternateColorCodes('&',
										plugin.getMessages().getConfig().getString("kits.select"))
						));
					}
				} else {
					meta.setLore(List.of(
							ChatColor.translateAlternateColorCodes('&',
									plugin.getMessages().getConfig().getString("kits.price").replace("%price%", String.valueOf(price))),
							ChatColor.translateAlternateColorCodes('&',
									plugin.getMessages().getConfig().getString("kits.buy"))
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
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
				plugin.getMenu().getConfig().getString("kits-menu.title"))))
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
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("kits.select-message").replace("%kitname%", kitName)));
		} else {
			if (playerCoins >= price) {
				plugin.getPlayerStatsManager().getStats(player.getUniqueId()).setCoins(playerCoins - price);
				databaseManager.addKit(player.getUniqueId(), kitName);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("kits.purchased").replace("%kitname%", kitName)));
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
						plugin.getMessages().getConfig().getString("kits.not-enough-coins")));
			}
		}

		player.closeInventory(); // Zavření menu
	}

}
