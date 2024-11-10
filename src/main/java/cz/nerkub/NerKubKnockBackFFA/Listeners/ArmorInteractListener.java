package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorInteractListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public ArmorInteractListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();

			// Získání kliknutého itemu a jeho slotu
			ItemStack clickedItem = event.getCurrentItem();

			// Zkontroluj, zda je to armor slot (boty, kalhoty, hrudník, helma)
			if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
				// Zrušíme jakýkoli pokus o přesunutí itemu do armor slotu
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		// Zamezíme hráči v tom, aby brnění vyhodil
		ItemStack droppedItem = event.getItemDrop().getItemStack();
		if (isArmorPiece(droppedItem)) {
			event.setCancelled(true);
		}
	}

	// Pomocná metoda pro kontrolu, zda je položka kusem brnění
	private boolean isArmorPiece(ItemStack item) {
		Material type = item.getType();
		return type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS ||
				type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_CHESTPLATE || type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_BOOTS ||
				type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE || type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS ||
				type == Material.GOLDEN_HELMET || type == Material.GOLDEN_CHESTPLATE || type == Material.GOLDEN_LEGGINGS || type == Material.GOLDEN_BOOTS ||
				type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE || type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS ||
				type == Material.NETHERITE_HELMET || type == Material.NETHERITE_CHESTPLATE || type == Material.NETHERITE_LEGGINGS || type == Material.NETHERITE_BOOTS;
	}
}
