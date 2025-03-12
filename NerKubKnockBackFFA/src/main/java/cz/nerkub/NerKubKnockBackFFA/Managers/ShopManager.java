package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Items.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
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
	private final InvisibilityCloakItem invisibilityCloakItem;
	private final FireBallLauncherItem fireBallLauncherItem;
	private final ExplodingChickItem explodingChickItem;
	private final BlazingDashItem blazingDashItem;
	private final SpeedBoostItem speedBoostItem;

	private final PlayerStatsManager playerStatsManager;

	public ShopManager(NerKubKnockBackFFA plugin, LevitationBootsItem levitationBootsItem, SwapperBallItem swapperBallItem, InvisibilityCloakItem invisibilityCloakItem, FireBallLauncherItem fireBallLauncherItem, ExplodingChickItem explodingChickItem, BlazingDashItem blazingDashItem, SpeedBoostItem speedBoostItem, PlayerStatsManager playerStatsManager) {
		this.plugin = plugin;
		this.levitationBootsItem = levitationBootsItem;
		this.swapperBallItem = swapperBallItem; // Inicializace
		this.invisibilityCloakItem = invisibilityCloakItem;
		this.fireBallLauncherItem = fireBallLauncherItem;
		this.explodingChickItem = explodingChickItem;
		this.blazingDashItem = blazingDashItem;
		this.speedBoostItem = speedBoostItem;
		this.playerStatsManager = playerStatsManager;
	}

	public void openShop(Player player) {
		Inventory shopInventory = Bukkit.createInventory(null, plugin.getShop().getConfig().getInt("size", 27), ChatColor.translateAlternateColorCodes('&', plugin.getShop().getConfig().getString("title")));

		addBorder(shopInventory);
		addItemsToShop(shopInventory);
		player.openInventory(shopInventory); // Otevření inventáře pro hráče
	}

	private void addBorder(Inventory inventory) {
		// Načtení materiálu z configu
		String materialName = plugin.getShop().getConfig().getString("filler", "GRAY_STAINED_GLASS_PANE");
		Material material;

		// Ověření, zda materiál existuje
		try {
			material = Material.valueOf(materialName.toUpperCase());
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().warning("⚠️ Invalid material in shop.yml: " + materialName + ". Using default GRAY_STAINED_GLASS_PANE.");
			material = Material.GRAY_STAINED_GLASS_PANE; // Výchozí materiál, pokud je neplatný
		}

		// Vytvoření itemu
		ItemStack filler = new ItemStack(material);
		ItemMeta meta = filler.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.GRAY + " ");
			filler.setItemMeta(meta);
		}

		int size = inventory.getSize();

		// Přidání do horní a spodní řady
		for (int i = 0; i < 9; i++) {
			inventory.setItem(i, filler); // Horní řada
			inventory.setItem(size - 9 + i, filler); // Spodní řada
		}

		// Přidání do levého a pravého sloupce
		for (int i = 0; i < size; i += 9) {
			inventory.setItem(i, filler); // Levý sloupec
			inventory.setItem(i + 8, filler); // Pravý sloupec
		}
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

		ItemStack invisibilityCloak = invisibilityCloakItem.createInvisibilityCloakItem();
		addItemWithPriceToLore(invisibilityCloak, "invisibility-cloak");
		shopInventory.addItem(invisibilityCloak);

		// Přidání Fireball Launcher
		ItemStack fireBallLauncher = fireBallLauncherItem.createFireBallLauncherItem();
		addItemWithPriceToLore(fireBallLauncher, "fireball-launcher");
		shopInventory.addItem(fireBallLauncher);

		ItemStack explodingChick = explodingChickItem.createExplodingChickItem();
		addItemWithPriceToLore(explodingChick, "exploding-chick");
		shopInventory.addItem(explodingChick);

		ItemStack blazingDash = blazingDashItem.createBlazingDashItem();
		addItemWithPriceToLore(blazingDash, "blazing-dash");
		shopInventory.addItem(blazingDash);

		ItemStack speedBoost = speedBoostItem.createSpeedBoostItem();
		addItemWithPriceToLore(speedBoost, "speed-boost");
		shopInventory.addItem(speedBoost);
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
			lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("shop.price").replace("%price%", String.valueOf(price))));
			meta.setLore(lore);
			item.setItemMeta(meta); // Aktualizace ItemMeta na item
		}
	}

	public void purchaseItem(Player player, ItemStack item, String itemKey) {
		int price = plugin.getShop().getConfig().getInt(itemKey + ".price");

		String prefix = plugin.getMessages().getConfig().getString("prefix");

		// Získání počtu coinů hráče
		int playerCoins = plugin.getPlayerStatsManager().getStats(player.getUniqueId()).getCoins();

		// Kontrola, zda má hráč dostatek coinů
		if (playerCoins >= price) {
			// Odečti coiny
			plugin.getPlayerStatsManager().getStats(player.getUniqueId()).setCoins(playerCoins - price);

			// Přidání itemu do inventáře hráče
			player.getInventory().addItem(item);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
					plugin.getMessages().getConfig().getString("shop.purchased")).replace("%item%", item.getItemMeta().getDisplayName()).replace("%price%", String.valueOf(price)));
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("shop.not-enough-coins")));
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
		}
	}


}
