package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class LevitationBootsListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public LevitationBootsListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		// Zkontroluj, zda hráč kliknul pravým tlačítkem
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Získání předmětů z hlavní a vedlejší ruky
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();
			ItemStack offHandItem = player.getInventory().getItemInOffHand();

			// Zkontroluj, zda má hráč kožené boty v hlavní nebo vedlejší ruce
			if (mainHandItem.getType() == Material.LEATHER_BOOTS || offHandItem.getType() == Material.LEATHER_BOOTS) {
				// Aplikuj efekt levitace
				player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
						plugin.getItems().getConfig().getInt("levitation-boots.levitation.duration") * 20,
						plugin.getItems().getConfig().getInt("levitation-boots.levitation.effect") - 1, true));

				// Snížení počtu bot
				if (mainHandItem.getType() == Material.LEATHER_BOOTS) {
					mainHandItem.setAmount(mainHandItem.getAmount() - 1);
				} else if (offHandItem.getType() == Material.LEATHER_BOOTS) {
					offHandItem.setAmount(offHandItem.getAmount() - 1);
				}

			}
		}
	}
}
