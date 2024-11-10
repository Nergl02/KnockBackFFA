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

public class KnockBackStickItem {

	private final NerKubKnockBackFFA plugin;

	public KnockBackStickItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createKnockBackStickItem() {
		ItemStack knockBackStick = new ItemStack(Material.valueOf(plugin.getItems().getConfig().getString("knockback-stick.material").toUpperCase()));
		ItemMeta knockBackStickMeta = knockBackStick.getItemMeta();

		knockBackStickMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("knockback-stick.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("knockback-stick.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		knockBackStickMeta.addEnchant(Enchantment.KNOCKBACK, plugin.getItems().getConfig().getInt("knockback-stick.knockback"), true);

		if (plugin.getItems().getConfig().getBoolean("knockback-stick.hide-enchantment")) {
			knockBackStickMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}


		knockBackStickMeta.setLore(lore);
		knockBackStick.setItemMeta(knockBackStickMeta);

		return knockBackStick;
	}
}
