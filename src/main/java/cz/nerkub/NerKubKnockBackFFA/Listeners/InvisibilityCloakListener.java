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
import org.bukkit.scheduler.BukkitRunnable;

public class InvisibilityCloakListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public InvisibilityCloakListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract (PlayerInteractEvent event){
		Player player = event.getPlayer();
		String invisibilityCloakItem = plugin.getItems().getConfig().getString("invisibility-cloak.material").toUpperCase();

		// Check if player use right hand when click
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();
			ItemStack offHandItem = player.getInventory().getItemInOffHand();

			if (mainHandItem.getType() == Material.valueOf(invisibilityCloakItem) || offHandItem.getType() == Material.valueOf(invisibilityCloakItem)){
				int duration = plugin.getItems().getConfig().getInt("invisibility-cloak.invisibility.duration") * 20;
				boolean removeArmor = plugin.getItems().getConfig().getBoolean("invisibility-cloak.invisibility.remove-armor");

				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
						duration, 1,true));

				// Uložení brnění
				ItemStack[] armorContents = null;
				if (removeArmor) {
					armorContents = player.getInventory().getArmorContents();
					player.getInventory().setArmorContents(new ItemStack[] {null, null, null, null}); // Odstranění brnění
				}

				if (mainHandItem.getType() == Material.valueOf(invisibilityCloakItem)) {
					mainHandItem.setAmount(mainHandItem.getAmount() - 1);
				} else if (offHandItem.getType() == Material.valueOf(invisibilityCloakItem)) {
					offHandItem.setAmount(offHandItem.getAmount() - 1);
				}

				// Pokud bylo brnění odebráno, tato metoda ho vrátí zpět po uplynutí efektu
				if (removeArmor) {
					ItemStack [] finalArmorContents = armorContents;
					new BukkitRunnable() {

						@Override
						public void run() {
							player.getInventory().setArmorContents(finalArmorContents);
						}
					}.runTaskLater(plugin, duration);
				}

			}
		}
	}

}
