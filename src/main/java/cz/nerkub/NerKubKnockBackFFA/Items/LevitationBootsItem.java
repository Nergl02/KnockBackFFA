package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class LevitationBootsItem {

	private final NerKubKnockBackFFA plugin;

	public LevitationBootsItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createLevitationBootsItem() {
		ItemStack levitationBootsItem = new ItemStack(Material.LEATHER_BOOTS);
		ItemMeta levitationBootsMeta = levitationBootsItem.getItemMeta();

		if (levitationBootsMeta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) levitationBootsMeta;

			leatherArmorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					plugin.getItems().getConfig().getString("levitation-boots.display-name")));

			List<String> loreList = plugin.getItems().getConfig().getStringList("levitation-boots.lore");
			List<String> lore = new ArrayList<>();
			for (String line : loreList) {
				lore.add(ChatColor.translateAlternateColorCodes('&', line));
			}

			leatherArmorMeta.addEnchant(Enchantment.DURABILITY, 1, true);

			if (plugin.getItems().getConfig().getBoolean("levitation-boots.hide-enchantment")) {
				leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			leatherArmorMeta.addItemFlags(ItemFlag.HIDE_DYE);

			// Nastavení barvy bot
			leatherArmorMeta.setColor(Color.fromRGB(173, 216, 230));

			leatherArmorMeta.setLore(lore);
			levitationBootsItem.setItemMeta(leatherArmorMeta);
		}

		return levitationBootsItem;
	}

	public String getDisplayName() {
		// Získání a překlad názvu předmětu přímo z konfigurace
		return ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("levitation-boots.display-name"));
	}
}
