package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PlayerMenuManager {

	private final NerKubKnockBackFFA plugin;


	public PlayerMenuManager(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public void openMenu (Player player) {
		Inventory menuInventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&8Player's Menu"));

		player.openInventory(menuInventory);
	}
}
