package cz.nerkub.NerKubKnockBackFFA.Items;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class FireBallLauncherItem implements Listener {

	private final NerKubKnockBackFFA plugin;

	public FireBallLauncherItem(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public ItemStack createFireBallLauncherItem() {
		ItemStack fireBallLauncherItem = new ItemStack(Material.BLAZE_ROD);
		ItemMeta fireBallLauncherMeta = fireBallLauncherItem.getItemMeta();

		fireBallLauncherMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getItems().getConfig().getString("fireball-launcher.display-name")));

		List<String> loreList = plugin.getItems().getConfig().getStringList("fireball-launcher.lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreList) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		fireBallLauncherMeta.addEnchant(Enchantment.DURABILITY, 1, true);

		if (plugin.getItems().getConfig().getBoolean("fireball-launcher.hide-enchantment")) {
			fireBallLauncherMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		fireBallLauncherMeta.setLore(lore);
		fireBallLauncherItem.setItemMeta(fireBallLauncherMeta);

		return fireBallLauncherItem;
	}


}
