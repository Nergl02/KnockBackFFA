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

public class SwapperBallItem {

	private final NerKubKnockBackFFA plugin;


	public SwapperBallItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}


	public ItemStack createSwapperBallItem() {
		ItemStack swapperBallItem = new ItemStack(Material.SNOWBALL);
		ItemMeta swapperBallMeta = swapperBallItem.getItemMeta();

		swapperBallMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("swapper-ball.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("swapper-ball.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		swapperBallMeta.addEnchant(Enchantment.DURABILITY, 1, true);

		if (plugin.getItems().getConfig().getBoolean("swapper-ball.hide-enchantment")) {
			swapperBallMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		swapperBallMeta.setLore(lore);
		swapperBallItem.setItemMeta(swapperBallMeta);

		return swapperBallItem;
	}
}
