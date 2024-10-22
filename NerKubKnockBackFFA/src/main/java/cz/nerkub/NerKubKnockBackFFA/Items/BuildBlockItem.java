package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BuildBlockItem {

	private final NerKubKnockBackFFA plugin;

	public BuildBlockItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createBuildBlockItem (int amount) {
		ItemStack buildBlockItem = new ItemStack(Material.valueOf(plugin.getItems().getConfig().getString("build-block.material").toUpperCase()), amount);
		ItemMeta buildBlockMeta = buildBlockItem.getItemMeta();

		buildBlockMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("build-block.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("build-block.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		buildBlockMeta.setLore(lore);
		buildBlockItem.setItemMeta(buildBlockMeta);


		return buildBlockItem;
	}
}
