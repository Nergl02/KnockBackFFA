package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ExtraPunchBowEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;
	private boolean eventActive = true;

	private final Map<UUID, ItemStack> originalBows = new HashMap<>();
	private final Set<UUID> processedPlayers = new HashSet<>();
	private final Set<UUID> playersToReturnBow = new HashSet<>();

	public ExtraPunchBowEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = plugin.getArenaManager();

		// Always read from latest config
		int duration = plugin.getEvents().getConfig().getInt("event-settings.event-duration", 60);
		String startMessage = plugin.getEvents().getConfig().getString("events.extra-punch-bow.message-start", "&6🏹 ExtraPunchBow Event is active!");
		String endMessage = plugin.getEvents().getConfig().getString("events.extra-punch-bow.message-end", "&a🏹 ExtraPunchBow Event has ended!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));
		applyExtraPunch();

		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false;
				removeExtraPunch();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null);
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	private void applyExtraPunch() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
				giveExtraPunchBow(player);
			}
		}
	}

	public void giveExtraPunchBow(Player player) {
		UUID playerId = player.getUniqueId();
		if (processedPlayers.contains(playerId)) return;

		int punchLevel = plugin.getEvents().getConfig().getInt("events.extra-punch-bow.punch-level", 5);

		ItemStack[] contents = player.getInventory().getContents();
		boolean foundBow = false;

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType() == Material.BOW) {
				originalBows.put(playerId, item.clone());
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

		if (!foundBow) {
			ItemStack newBow = createExtraPunchBow(punchLevel);
			player.getInventory().addItem(newBow);
			originalBows.put(playerId, null);
		}

		processedPlayers.add(playerId);
	}

	public ItemStack createExtraPunchBow(int punchLevel) {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta meta = bow.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.GOLD + "Extra Punch Bow");
			meta.addEnchant(Enchantment.ARROW_KNOCKBACK, punchLevel, true);
			bow.setItemMeta(meta);
		}
		return bow;
	}

	private void removeExtraPunch() {
		Set<UUID> playerIds = new HashSet<>(originalBows.keySet());

		for (UUID playerId : playerIds) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
					giveOriginalBow(player);
					plugin.getCustomEventManager().removePlayerFromEvent(playerId, "ExtraPunchBow");
				} else {
					plugin.getCustomEventManager().scheduleExtraPunchBowReturn(playerId);
				}
			}
		}

		originalBows.clear();
		processedPlayers.clear();
	}

	private void giveOriginalBow(Player player) {
		UUID playerId = player.getUniqueId();

		if (!originalBows.containsKey(playerId)) {
			removeExtraPunchBow(player);
			return;
		}

		ItemStack originalBow = originalBows.get(playerId);
		boolean restored = false;

		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (item != null && isExtraPunchBow(item)) {
				if (originalBow != null) {
					player.getInventory().setItem(i, originalBow);
				} else {
					player.getInventory().setItem(i, new ItemStack(Material.AIR));
				}
				restored = true;
				break;
			}
		}

		if (!restored) {
			removeExtraPunchBow(player);
		}

		originalBows.remove(playerId);
		processedPlayers.remove(playerId);
	}

	private void removeExtraPunchBow(Player player) {
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (item != null && isExtraPunchBow(item)) {
				player.getInventory().setItem(i, new ItemStack(Material.AIR));
			}
		}
		player.updateInventory();
	}

	public void resetProcessedPlayer(UUID playerId) {
		processedPlayers.remove(playerId);
	}

	private boolean isExtraPunchBow(ItemStack item) {
		if (item == null || item.getType() != Material.BOW || !item.hasItemMeta()) return false;
		ItemMeta meta = item.getItemMeta();
		return meta.hasEnchant(Enchantment.ARROW_KNOCKBACK) &&
				meta.getDisplayName().equals(ChatColor.GOLD + "Extra Punch Bow");
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		if (eventActive && !processedPlayers.contains(playerId) &&
				!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
			giveExtraPunchBow(player);
		}

		if (playersToReturnBow.contains(playerId)) {
			giveOriginalBow(player);
			playersToReturnBow.remove(playerId);
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
