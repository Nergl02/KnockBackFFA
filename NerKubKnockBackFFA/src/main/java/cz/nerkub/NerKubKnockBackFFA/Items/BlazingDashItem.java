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

public class BlazingDashItem {

	private final NerKubKnockBackFFA plugin;

	public BlazingDashItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createBlazingDashItem() {
		ItemStack blazingDashItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta blazingDashMeta = blazingDashItem.getItemMeta();

		blazingDashMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("blazing-dash.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("blazing-dash.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		blazingDashMeta.addEnchant(Enchantment.DURABILITY, 1, true);

		if (plugin.getItems().getConfig().getBoolean("blazing-dash.hide-enchantments")) {
			blazingDashMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		blazingDashMeta.setLore(lore);
		blazingDashItem.setItemMeta(blazingDashMeta);

		return blazingDashItem;
	}
}
