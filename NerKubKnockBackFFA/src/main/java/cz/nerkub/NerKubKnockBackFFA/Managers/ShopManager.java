package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Items.LevitationBootsItem;
import cz.nerkub.NerKubKnockBackFFA.Items.SwapperBallItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class ShopManager {
	private final NerKubKnockBackFFA plugin;
	private final LevitationBootsItem levitationBootsItem;
	private final SwapperBallItem swapperBallItem; // Přidání itemu pro SwapperBall

	public ShopManager(NerKubKnockBackFFA plugin, LevitationBootsItem levitationBootsItem, SwapperBallItem swapperBallItem) {
		this.plugin = plugin;
		this.levitationBootsItem = levitationBootsItem;
		this.swapperBallItem = swapperBallItem; // Inicializace
	}

	public void openShop(Player player) {
		Inventory shopInventory = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', plugin.getShop().getConfig().getString("title")));

		addItemsToShop(shopInventory);
		player.openInventory(shopInventory); // Otevření inventáře pro hráče
	}

	public void addItemsToShop(Inventory shopInventory) {
		// Přidání Levitation Boots
		ItemStack levitationBoots = levitationBootsItem.createLevitationBootsItem();
		addItemWithPriceToLore(levitationBoots, "levitation-boots");
		shopInventory.addItem(levitationBoots);

		// Přidání Swapper Ball
		ItemStack swapperBall = swapperBallItem.createSwapperBallItem(); // Předpokládáme, že máte metodu pro vytvoření SwapperBall
		addItemWithPriceToLore(swapperBall, "swapper-ball");
		shopInventory.addItem(swapperBall);
	}

	private void addItemWithPriceToLore(ItemStack item, String itemKey) {
		FileConfiguration shopConfig = plugin.getShop().getConfig();
		int price = shopConfig.getInt(itemKey + ".price");

		// Získání ItemMeta a přidání ceny do lore
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore == null) {
				lore = new ArrayList<>();
			}
			lore.add("");
			lore.add(ChatColor.translateAlternateColorCodes('&', "&aPrice: &e" + price + " &acoins"));
			meta.setLore(lore);
			item.setItemMeta(meta); // Aktualizace ItemMeta na item
		}
	}

	public void purchaseItem(Player player, ItemStack item, String itemKey) {
		FileConfiguration playersConfig = plugin.getPlayers().getConfig(); // Zde získáváte config s hráči
		int price = plugin.getShop().getConfig().getInt(itemKey + ".price");

		// Získání počtu coinů hráče
		int playerCoins = playersConfig.getInt(player.getDisplayName() + ".coins");

		// Kontrola, zda má hráč dostatek coinů
		if (playerCoins >= price) {
			// Odečti coiny
			playersConfig.set(player.getDisplayName() + ".coins", playerCoins - price);
			plugin.getPlayers().saveConfig(); // Uložení změn

			// Přidání itemu do inventáře hráče
			player.getInventory().addItem(item);
			player.sendMessage(ChatColor.GREEN + "You have successfully purchased " + item.getItemMeta().getDisplayName() + " for " + price + " coins.");
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		} else {
			player.sendMessage(ChatColor.RED + "You do not have enough coins to purchase this item!");
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
		}
	}


}
