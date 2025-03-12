package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class NoKnockBackStickEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final Map<Player, Boolean> hadKnockBackStick = new HashMap<>();
	private final KnockBackStickItem knockBackStickItem;
	private boolean eventActive = true; // Flag pro aktivní event

	public NoKnockBackStickEvent(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;

		// 📜 Načtení zpráv z `events.yml`
		String startMessage = ChatColor.translateAlternateColorCodes('&', plugin.getEvents().getConfig().getString("events.no-knockback-stick.message-start"));
		String endMessage = ChatColor.translateAlternateColorCodes('&', plugin.getEvents().getConfig().getString("events.no-knockback-stick.message-end"));

		Bukkit.broadcastMessage(startMessage);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		// Odebrání KnockBack Sticků hráčům
		for (Player player : Bukkit.getOnlinePlayers()) {
			removeKnockBackStick(player);
		}

		// ⏳ Čas trvání eventu
		int duration = plugin.getEvents().getConfig().getInt("event-settings.event-duration");

		// 🕒 Naplánování návratu KnockBack Sticků po konci eventu
		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false; // Deaktivace eventu
				restoreKnockBackStick();
				Bukkit.broadcastMessage(endMessage);
				plugin.getCustomEventManager().setCurrentEvent(null); // Ukončení eventu
			}
		}.runTaskLater(plugin, duration * 20L); // Převod sekund na ticky
	}

	private void removeKnockBackStick(Player player) {
		ItemStack[] contents = player.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType() == Material.STICK) {
				// Ověříme, zda jde o KnockBack Stick
				if (item.getItemMeta() != null &&
						item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
								plugin.getItems().getConfig().getString("knockback-stick.display-name")))) {
					player.getInventory().setItem(i, null); // Odstranění
					hadKnockBackStick.put(player, true); // Uložení, že hráč měl stick
				}
			}
		}
		player.updateInventory();
	}

	private void restoreKnockBackStick() {
		for (Player player : hadKnockBackStick.keySet()) {
			if (hadKnockBackStick.get(player)) {
				player.getInventory().addItem(knockBackStickItem.createKnockBackStickItem()); // Vrácení Sticku
			}
		}
		hadKnockBackStick.clear(); // Vyčištění mapy po eventu
	}

	// 🏆 Pokud se hráč připojí během eventu, dostane KnockBack Stick zpět po skončení
	@org.bukkit.event.EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (eventActive) {
			Player player = event.getPlayer();
			hadKnockBackStick.put(player, true); // Označení, že hráč by měl dostat Stick zpět
			removeKnockBackStick(player); // Odebrání KnockBack Sticku, pokud ho má při joinu
		}
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
