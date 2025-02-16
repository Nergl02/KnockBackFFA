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

public class InvisibilityCloakItem {

	private final NerKubKnockBackFFA plugin;

	public InvisibilityCloakItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createInvisibilityCloakItem() {
		ItemStack invisibilityCloakItem = new ItemStack(Material.valueOf(plugin.getItems().getConfig().getString("invisibility-cloak.material").toUpperCase()));
		ItemMeta invisibilityCloakMeta = invisibilityCloakItem.getItemMeta();

		invisibilityCloakMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("invisibility-cloak.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("invisibility-cloak.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		invisibilityCloakMeta.addEnchant(Enchantment.DURABILITY, 1, true);

		if (plugin.getItems().getConfig().getBoolean("invisibility-cloak.hide-enchantment")){
			invisibilityCloakMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		invisibilityCloakMeta.setLore(lore);
		invisibilityCloakItem.setItemMeta(invisibilityCloakMeta);

		return invisibilityCloakItem;
	}

}
