package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedBoostListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public SpeedBoostListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String speedBoostItem = plugin.getItems().getConfig().getString("speed-boost.material").toUpperCase();

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();
			ItemStack offHandItem = player.getInventory().getItemInOffHand();

			if (mainHandItem.getType() == Material.valueOf(speedBoostItem) || offHandItem.getType() == Material.valueOf(speedBoostItem)) {
				int duration = plugin.getItems().getConfig().getInt("speed-boost.speed.duration") * 20;
				int amplifier = plugin.getItems().getConfig().getInt("speed-boost.speed.effect");

				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
						duration, amplifier, true));
			}

			if (mainHandItem.getType() == Material.valueOf(speedBoostItem)) {
				mainHandItem.setAmount(mainHandItem.getAmount() - 1);
			} else if (offHandItem.getType() == Material.valueOf(speedBoostItem)) {
				offHandItem.setAmount(offHandItem.getAmount() - 1);
			}
		}
	}
}
