package cz.nerkub.NerKubKnockBackFFA.Events;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SafeZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExtraPunchBowEvent extends Event implements Listener {

	private static final HandlerList handlers = new HandlerList();
	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;
	private final int duration;
	private final int punchLevel;
	private final String startMessage;
	private final String endMessage;

	private final Map<UUID, ItemStack> originalBows = new HashMap<>();
	// Set pro hráče, kteří už obdrželi efekt během aktuálního eventu
	private final Set<UUID> processedPlayers = new HashSet<>();
	private final Set<UUID> playersToReturnBow = new HashSet<>(); // 📌 Seznam hráčů, kterým máme vrátit luk
	private boolean eventActive = true; // Flag pro aktivní event

	public ExtraPunchBowEvent(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = plugin.getArenaManager();

		FileConfiguration config = plugin.getEvents().getConfig();
		this.duration = config.getInt("event-settings.event-duration", 60);
		this.punchLevel = config.getInt("events.extra-punch-bow.punch-level", 5);
		this.startMessage = config.getString("events.extra-punch-bow.message-start", "&6🏹 ExtraPunchBow Event is active! All bows have extra knockback!");
		this.endMessage = config.getString("events.extra-punch-bow.message-end", "&a🏹 ExtraPunchBow Event has ended! Bows are back to normal!");

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMessage));
		// Aplikujeme efekt hráčům mimo safezónu
		applyExtraPunch();

		new BukkitRunnable() {
			@Override
			public void run() {
				eventActive = false; // Deaktivace eventu
				removeExtraPunch();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
				plugin.getCustomEventManager().setCurrentEvent(null); // Ukončení eventu
			}
		}.runTaskLater(plugin, duration * 20L);
	}

	// Přidá efekt Extra Punch Bow pouze hráčům, kteří nejsou v safezóně
	private void applyExtraPunch() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
				giveExtraPunchBow(player);
			}
		}
	}

	// Dává hráči Extra Punch Bow a uloží původní luk (pokud existoval)
	public void giveExtraPunchBow(Player player) {
		UUID playerId = player.getUniqueId();
		// Pokud už hráč efekt dostal, neaplikujeme ho znovu
		if (processedPlayers.contains(playerId)) {
			return;
		}

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
			ItemStack newBow = createExtraPunchBow();
			player.getInventory().addItem(newBow);
			originalBows.put(playerId, null);
		}
		processedPlayers.add(playerId);
	}

	// Vytvoří nový Extra Punch Bow
	public ItemStack createExtraPunchBow() {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta meta = bow.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.GOLD + "Extra Punch Bow");
			meta.addEnchant(Enchantment.ARROW_KNOCKBACK, punchLevel, true);
			bow.setItemMeta(meta);
		}
		return bow;
	}

	// Odstraní Extra Punch Bow a obnoví původní luky
	private void removeExtraPunch() {
		// 📌 Vytvoříme kopii klíčů, abychom mohli iterovat bez chyby
		Set<UUID> playerIds = new HashSet<>(originalBows.keySet());

		for (UUID playerId : playerIds) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				if (!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
					// ✅ Hráč je mimo safezóny → dostane zpět svůj luk
					giveOriginalBow(player);
					plugin.getCustomEventManager().removePlayerFromEvent(playerId, "ExtraPunchBow");
				} else {
					// ❌ Hráč je stále v safezóně → uložíme ho do seznamu pro vrácení luku po opuštění
					plugin.getCustomEventManager().scheduleExtraPunchBowReturn(playerId);
				}
			}
		}

		// Po iteraci teprve vyčistíme mapu
		originalBows.clear();
		processedPlayers.clear();
	}


	private void giveOriginalBow(Player player) {
		UUID playerId = player.getUniqueId();

		// 📌 Pokud hráč původní luk neměl, pouze odstraníme Extra Punch Bow
		if (!originalBows.containsKey(playerId)) {
			removeExtraPunchBow(player);
			return;
		}

		ItemStack originalBow = originalBows.get(playerId);
		boolean restored = false;

		// 🔄 Projdeme inventář a obnovíme luk pouze tam, kde byl Extra Punch Bow
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);

			if (item != null && isExtraPunchBow(item)) {
				if (originalBow != null) {
					player.getInventory().setItem(i, originalBow);
				} else {
					player.getInventory().setItem(i, new ItemStack(Material.AIR)); // ✅ Odstraníme Extra Punch Bow
				}
				restored = true;
				break; // 🚀 Jakmile obnovíme jeden luk, nemusíme prohledávat dál
			}
		}

		// ✅ Pokud hráč neměl Extra Punch Bow v inventáři, jen se smaže ze seznamů
		if (!restored) {
			removeExtraPunchBow(player);
		}

		// ❌ Vyčištění záznamů pro tohoto hráče
		originalBows.remove(playerId);
		processedPlayers.remove(playerId);
	}

	private void removeExtraPunchBow(Player player) {
		// 🔄 Projdeme celý inventář a odstraníme všechny Extra Punch Bow
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (item != null && isExtraPunchBow(item)) {
				player.getInventory().setItem(i, new ItemStack(Material.AIR)); // ✅ Odstraníme Extra Punch Bow
			}
		}
		player.updateInventory(); // ✅ Aktualizace inventáře
	}

	public void resetProcessedPlayer(UUID playerId) {
		processedPlayers.remove(playerId);
	}


	// Kontroluje, zda je daný item Extra Punch Bow
	private boolean isExtraPunchBow(ItemStack item) {
		if (item == null || item.getType() != Material.BOW || !item.hasItemMeta()) return false;
		ItemMeta meta = item.getItemMeta();
		return meta.hasEnchant(Enchantment.ARROW_KNOCKBACK) &&
				meta.getDisplayName().equals(ChatColor.GOLD + "Extra Punch Bow");
	}

	// Pokud hráč opustí safezónu během eventu, aplikuje se mu efekt, pokud ještě nedostal
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		if (eventActive && !processedPlayers.contains(playerId) &&
				!safeZoneManager.isInSafeZone(player.getLocation(), arenaManager.getArenaSpawn(arenaManager.getCurrentArenaName()))) {
			giveExtraPunchBow(player);
		}

// ✅ Pokud byl hráč v safezóně při konci eventu, teď dostane luk
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
