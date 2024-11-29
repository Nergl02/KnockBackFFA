package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;


public class MaxItemInInvListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public MaxItemInInvListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	// Metoda pro omezení počtu předmětů v inventáři jednoho hráče
	public void limitItemCount(Player player, Material material, int maxCount) {
		int totalItems = 0;

		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType() == material) {
				int stackAmount = item.getAmount();

				if (totalItems + stackAmount > maxCount) {
					int allowedAmount = maxCount - totalItems;
					item.setAmount(Math.max(allowedAmount, 0)); // Nastaví množství na max povolený počet
					totalItems = maxCount;
				} else {
					totalItems += stackAmount;
				}

				if (totalItems >= maxCount) {
					break; // Překročení limitu, ukončíme cyklus
				}
			}
		}
	}

	// Metoda pro kontrolu konkrétního hráče, volatelná při událostech
	public void checkPlayerInventory(Player player) {
		limitItemCount(player, Material.ENDER_PEARL, plugin.getConfig().getInt("max-ender-pearls") - 1);
		limitItemCount(player, Material.ARROW, plugin.getConfig().getInt("max-arrows") - 1);
		limitItemCount(player, Material.valueOf(plugin.getItems().getConfig().getString("build-block.material")), plugin.getConfig().getInt("build-blocks.max-amount")
				- plugin.getConfig().getInt("build.blocks.default-amount"));
	}
}



