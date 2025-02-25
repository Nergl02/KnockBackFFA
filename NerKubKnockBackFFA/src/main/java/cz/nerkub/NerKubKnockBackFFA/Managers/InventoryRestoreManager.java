package cz.nerkub.NerKubKnockBackFFA.Managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryRestoreManager {

	private Map<String, ItemStack[]> playerInventories = new HashMap<>();
	private Map<String, Location> playerLocations = new HashMap<>(); // Mapa pro uložení lokace hráče

	// Uloží inventář hráče
	public void saveInventory(Player player) {
		playerInventories.put(player.getName(), player.getInventory().getContents());
	}

	// Uloží pozici hráče
	public void saveLocation(Player player) {
		playerLocations.put(player.getName(), player.getLocation());
	}

	// Obnoví inventář hráče
	public void restoreInventory(Player player) {
		if (playerInventories.containsKey(player.getName())) {
			player.getInventory().setContents(playerInventories.get(player.getName()));
		}
	}

	// Obnoví pozici hráče
	public void restoreLocation(Player player) {
		if (playerLocations.containsKey(player.getName())) {
			player.teleport(playerLocations.get(player.getName())); // Teleport hráče zpět na původní pozici
		}
	}
}
