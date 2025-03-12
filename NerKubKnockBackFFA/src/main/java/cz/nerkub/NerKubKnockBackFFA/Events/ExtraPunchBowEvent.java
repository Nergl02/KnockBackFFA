package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ExtraPunchBowEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final int duration;
	private final int punchLevel;
	private final String startMessage;
	private final String endMessage;

	private final Map<Player, ItemStack> originalBows = new HashMap<>();

	public ExtraPunchBowEvent(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;

		FileConfiguration config = plugin.getEvents().getConfig();
		this.duration = config.getInt("event-settings.event-duration", 60);
		this.punchLevel = config.getInt("events.extra-punch-bow.punch-level", 5);
		this.startMessage = config.getString("events.extra-punch-bow.message-start", "&6🏹 ExtraPunchBow Event is active! All bows have extra knockback!");
		this.endMessage = config.getString("events.extra-punch-bow.message-end", "&a🏹 ExtraPunchBow Event has ended! Bows are back to normal!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

		applyExtraPunch();

		new BukkitRunnable() {
			@Override
			public void run() {
				removeExtraPunch();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null); // Ukončení eventu
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	// 🔥 Přidání Extra Punch Bow
	private void applyExtraPunch() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			ItemStack[] contents = player.getInventory().getContents();
			boolean foundBow = false;

			for (int i = 0; i < contents.length; i++) {
				ItemStack item = contents[i];

				if (item != null && item.getType() == Material.BOW) {
					// Uložíme původní luk
					originalBows.put(player, item.clone());

					// Upgradujeme luk na Extra Punch Bow
					ItemStack upgradedBow = item.clone();
					ItemMeta meta = upgradedBow.getItemMeta();
					if (meta != null) {
						meta.addEnchant(Enchantment.ARROW_KNOCKBACK, punchLevel, true);
						meta.setDisplayName(ChatColor.GOLD + "Extra Punch Bow");
						upgradedBow.setItemMeta(meta);
					}
					player.getInventory().setItem(i, upgradedBow);
					foundBow = true;
					break;
				}
			}

			// Pokud hráč luk neměl, dáme mu nový s Punch V a uložíme, že ho neměl
			if (!foundBow) {
				ItemStack newBow = createExtraPunchBow();
				player.getInventory().addItem(newBow);
				originalBows.put(player, null); // Hráč původně luk neměl
			}
		}
	}

	// 🎯 Vytvoří Extra Punch Bow s Knockbackem
	private ItemStack createExtraPunchBow() {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta meta = bow.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.GOLD + "Extra Punch Bow");
			meta.addEnchant(Enchantment.ARROW_KNOCKBACK, punchLevel, true);
			bow.setItemMeta(meta);
		}
		return bow;
	}

	// ❌ Odebrání Extra Punch Bow a návrat původních luků
	private void removeExtraPunch() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (originalBows.containsKey(player)) {
				ItemStack originalBow = originalBows.get(player);
				ItemStack[] contents = player.getInventory().getContents();

				// 🔍 Projdeme celý inventář
				for (int i = 0; i < contents.length; i++) {
					ItemStack item = contents[i];

					// 🎯 Pokud je to Extra Punch Bow, odstraníme ho
					if (item != null && isExtraPunchBow(item)) {
						if (originalBow != null) {
							player.getInventory().setItem(i, originalBow); // ✅ Vrácení původního luku
						} else {
							player.getInventory().setItem(i, new ItemStack(Material.AIR)); // ❌ Pokud hráč původně luk neměl, odstraníme ho
						}
					}
				}
			}
		}
		originalBows.clear();
	}

	// ✅ Pomocná metoda k identifikaci Extra Punch Bow
	private boolean isExtraPunchBow(ItemStack item) {
		if (item == null || item.getType() != Material.BOW || !item.hasItemMeta()) return false;

		ItemMeta meta = item.getItemMeta();
		return meta.hasEnchant(Enchantment.ARROW_KNOCKBACK) &&
				meta.getDisplayName().equals(ChatColor.GOLD + "Extra Punch Bow");
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
