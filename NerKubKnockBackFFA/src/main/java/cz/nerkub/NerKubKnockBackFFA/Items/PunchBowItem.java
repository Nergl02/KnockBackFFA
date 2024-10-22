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

public class PunchBowItem {

	private final NerKubKnockBackFFA plugin;

	public PunchBowItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createBowItem () {
		ItemStack bowItem = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bowItem.getItemMeta();

		bowMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("bow.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("bow.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, plugin.getItems().getConfig().getInt("bow.punch"), true);

		if (plugin.getItems().getConfig().getBoolean("bow.hide-enchantment")) {
			bowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		bowMeta.setUnbreakable(true);

		bowMeta.setLore(lore);
		bowItem.setItemMeta(bowMeta);

		return bowItem;
	}

}
