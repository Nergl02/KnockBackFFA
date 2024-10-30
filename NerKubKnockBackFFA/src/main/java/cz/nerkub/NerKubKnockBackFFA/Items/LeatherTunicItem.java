package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LeatherTunicItem {

	private final NerKubKnockBackFFA plugin;

	public LeatherTunicItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}


	public ItemStack createLeatherTunicItem() {
		ItemStack leatherTunicItem = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherTunicItem.getItemMeta();

		leatherArmorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("leather-tunic.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("leather-tunic.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		leatherArmorMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, plugin.getItems().getConfig().getInt("leather-tunic.projectile-protection"), true);

		if (plugin.getItems().getConfig().getBoolean("leather-tunic.hide-enchantment")) {
			leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		if (leatherArmorMeta != null) {
			// Vygenerování náhodné barvy (RGB složky)
			Random random = new Random();
			int red = random.nextInt(256);   // Hodnoty od 0 do 255
			int green = random.nextInt(256);
			int blue = random.nextInt(256);

			// Nastavení náhodné barvy
			Color randomColor = Color.fromRGB(red, green, blue);
			leatherArmorMeta.setColor(randomColor);
			leatherArmorMeta.setLore(lore);
			leatherArmorMeta.setUnbreakable(true);

			// Aplikace meta dat zpět na tuniku
			leatherTunicItem.setItemMeta(leatherArmorMeta);

		}
		return leatherTunicItem;
	}
}
