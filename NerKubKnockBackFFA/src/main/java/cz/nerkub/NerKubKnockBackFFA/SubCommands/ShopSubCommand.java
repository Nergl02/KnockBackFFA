package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ShopManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ShopSubCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	public final ShopManager shopManager;

	public ShopSubCommand(NerKubKnockBackFFA plugin, ShopManager shopManager) {
		this.plugin = plugin;
		this.shopManager = shopManager;
	}

	@Override
	public String getName() {
		return "shop";
	}

	@Override
	public String getDescription() {
		return "&7Opens a shop for player";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa shop";
	}

	@Override
	public boolean perform(Player player, String[] args) {
		shopManager.openShop(player);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") + plugin.getMessages().getConfig().getString("shop-open")));

		return true;
	}
}
