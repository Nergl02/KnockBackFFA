package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlazingDashListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private double jumpPower;


	public BlazingDashListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
		reloadConfigValues();
	}

	@EventHandler
	public void onBoostPowderUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Ověříme, zda drží Blaze Powder
			if (item.getType() == Material.BLAZE_POWDER) {
				event.setCancelled(true); // Zabránění jiným interakcím

				int power = plugin.getItems().getConfig().getInt("blazing-dash.power");

				// Přidáme rychlostní boost dopředu
				Vector direction = player.getLocation().getDirection().multiply(2); // Zesílení síly
				direction.setY(power);
				player.setVelocity(direction);

				// Zvuk efekt
				player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.5f);

				// Snížení počtu položek
				if (item.getAmount() > 1) {
					item.setAmount(item.getAmount() - 1);
				} else {
					player.getInventory().setItemInMainHand(null);
				}
			}
		}

	}

	public void reloadConfigValues() {
		this.jumpPower = plugin.getConfig().getDouble("blazing-dash.power", 1.0);
	}
}
