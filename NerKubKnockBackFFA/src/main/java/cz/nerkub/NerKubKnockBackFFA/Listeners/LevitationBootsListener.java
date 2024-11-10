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
import org.bukkit.scheduler.BukkitRunnable;


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
				int duration = plugin.getItems().getConfig().getInt("levitation-boots.levitation.duration") * 20; // Sekundy na Ticks
				int effectLevel = plugin.getItems().getConfig().getInt("levitation-boots.levitation.effect") - 1;

				player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, effectLevel, true));

				// Spustit časovač, který po uplynutí 'duration' odstraní boty
				new BukkitRunnable() {
					@Override
					public void run() {
						// Pokud hráč stále drží boty (kontrola, zda má boty ve slotu pro brnění)
						if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS) {
							player.getInventory().setBoots(null); // Seber boty
						}
					}
				}.runTaskLater(plugin, duration); // Po uplynutí 'duration' odstraní boty
			}
		}
	}
}
