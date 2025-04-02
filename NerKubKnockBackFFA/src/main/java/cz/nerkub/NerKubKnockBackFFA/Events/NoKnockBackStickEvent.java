package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoKnockBackStickEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;
	private final KnockBackStickItem knockBackStickItem;

	private final Map<UUID, Boolean> hadKnockBackStick = new HashMap<>();
	private boolean eventActive = true;

	public NoKnockBackStickEvent(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem, SafeZoneManager safeZoneManager) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = plugin.getArenaManager();

		String startMessage = getConfigMessage("events.no-knockback-stick.message-start");
		String endMessage = getConfigMessage("events.no-knockback-stick.message-end");
		int duration = plugin.getEvents().getConfig().getInt("event-settings.event-duration", 60);

		Bukkit.broadcastMessage(startMessage);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		// Odebrání KnockBack Sticků hráčům mimo safezónu
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
				removeKnockBackStick(player);
			}
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false;
				restoreKnockBackStick();
				Bukkit.broadcastMessage(endMessage);
				plugin.getCustomEventManager().setCurrentEvent(null);
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	private String getConfigMessage(String path) {
		String raw = plugin.getEvents().getConfig().getString(path, "&7[Missing message]");
		return ChatColor.translateAlternateColorCodes('&', raw);
	}

	// ❌ Odebere hráči KnockBack Stick a uloží, že ho měl
	public void removeKnockBackStick(Player player) {
		ItemStack[] contents = player.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType() == Material.STICK) {
				// Ověříme, zda jde o KnockBack Stick
				if (item.getItemMeta() != null &&
						item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
								plugin.getItems().getConfig().getString("knockback-stick.display-name")))) {
					player.getInventory().setItem(i, null); // Odstranění
					hadKnockBackStick.put(player.getUniqueId(), true); // Uložení, že hráč měl stick
				}
			}
		}
		player.updateInventory();
	}

	// ✅ Vrácení KnockBack Sticků hráčům, kteří ho měli nebo byli v safezóně
	public void restoreKnockBackStick() {
		for (UUID playerId : hadKnockBackStick.keySet()) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
					// ✅ Hráč NENÍ v safezóně → dostane Stick zpět
					player.getInventory().addItem(knockBackStickItem.createKnockBackStickItem());
					plugin.getCustomEventManager().removePlayerFromEvent(playerId, "NoKnockBackStick");
				} else {
					// ❌ Hráč je stále v safezóně → uložíme, že mu ho máme vrátit až ji opustí
					plugin.getCustomEventManager().markKnockBackStickForReturn(playerId);
				}
			}
		}

		// ❌ Vyčistíme seznam hráčů, kteří Stick měli, protože už je vrácen nebo uložen k pozdějšímu vrácení
		hadKnockBackStick.clear();
	}



	// 🏆 Pokud se hráč připojí během eventu, Stick se mu odebere, pokud je mimo safezóny
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (eventActive) {
			Player player = event.getPlayer();
			if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
				hadKnockBackStick.put(player.getUniqueId(), true); // Označení, že hráč by měl dostat Stick zpět
				removeKnockBackStick(player); // Odebrání KnockBack Sticku, pokud ho má při joinu
			}
		}
	}

	// 🎯 Pokud hráč opustí safezónu během eventu, Stick se mu odebere
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		// Pokud hráč opustí safezónu a nemá ještě záznam v `hadKnockBackStick`, odebereme mu stick
		if (eventActive && !hadKnockBackStick.containsKey(player.getUniqueId()) &&
				!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
			removeKnockBackStick(player);
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
