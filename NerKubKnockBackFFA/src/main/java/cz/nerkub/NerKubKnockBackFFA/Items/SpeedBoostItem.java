package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpeedBoostItem {

	private final NerKubKnockBackFFA plugin;

	public SpeedBoostItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createSpeedBoostItem() {
		ItemStack speedBoostItem = new ItemStack(Material.valueOf(plugin.getItems().getConfig().getString("speed-boost.material").toUpperCase()));
		ItemMeta speedBoostMeta = speedBoostItem.getItemMeta();

		speedBoostMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("speed-boost.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("speed-boost.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		speedBoostMeta.addEnchant(Enchantment.DURABILITY, 1, true);

		if (plugin.getItems().getConfig().getBoolean("speed-boost.hide-enchantments")) {
			speedBoostMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		speedBoostMeta.setLore(lore);
		speedBoostItem.setItemMeta(speedBoostMeta);

		return speedBoostItem;

	}
}
