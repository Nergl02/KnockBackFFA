package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ExplodingChickItem {

	private final NerKubKnockBackFFA plugin;

	public ExplodingChickItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createExplodingChickItem() {
		ItemStack explodingChickItem = new ItemStack(Material.EGG);
		ItemMeta explodingChickMeta = explodingChickItem.getItemMeta();

		explodingChickMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("exploding-chick.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("exploding-chick.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		explodingChickMeta.setLore(lore);
		explodingChickItem.setItemMeta(explodingChickMeta);

		return explodingChickItem;
	}
}
