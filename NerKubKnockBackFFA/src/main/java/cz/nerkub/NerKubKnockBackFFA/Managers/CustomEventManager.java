package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Events.ArrowStormEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.ExtraPunchBowEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.LowGravityEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.NoKnockBackStickEvent;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CustomEventManager {

	private final NerKubKnockBackFFA plugin;
	private final SafeZoneManager safeZoneManager;
	private final ArenaManager arenaManager;
	private final Random random = new Random();
	private Event currentEvent = null;
	private String lastEvent = null; // Poslední spuštěný event

	private final KnockBackStickItem knockBackStickItem;

	private final boolean enableRandomEvents;
	private final int minDelay;
	private final int maxDelay;
	private final int eventDuration;
	final List<String> eventList = Arrays.asList("ArrowStorm", "NoKnockBackStick", "LowGravity", "ExtraPunchBow");
	private final Map<UUID, Set<String>> activePlayerEvents = new HashMap<>();
	private final Set<UUID> knockBackStickReturnList = new HashSet<>();
	private final Set<UUID> playersToReturnExtraPunchBow = new HashSet<>();

	public CustomEventManager(NerKubKnockBackFFA plugin, SafeZoneManager safeZoneManager, ArenaManager arenaManager, KnockBackStickItem knockBackStickItem) {
		this.plugin = plugin;
		this.safeZoneManager = safeZoneManager;
		this.arenaManager = arenaManager;
		this.knockBackStickItem = knockBackStickItem;

		FileConfiguration config = plugin.getEvents().getConfig();
		this.enableRandomEvents = config.getBoolean("event-settings.enable-random-events", true);
		this.minDelay = config.getInt("event-settings.min-delay", 300);
		this.maxDelay = config.getInt("event-settings.max-delay", 600);
		this.eventDuration = config.getInt("event-settings.event-duration", 60);

		if (enableRandomEvents) {
			startRandomEventTimer();
		}
	}

	private void startRandomEventTimer() {
		if (isEventActive()) return; // ✅ Nebude se volat, pokud už event běží

		int delay = (minDelay + random.nextInt(maxDelay - minDelay + 1)) * 20;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isEventActive()) { // ✅ Zajistí, že se opravdu spustí nový event
					startRandomEvent();
				}
			}
		}.runTaskLater(plugin, delay);
	}

	private void startRandomEvent() {
		if (isEventActive()) return; // ✅ Zabráníme vícenásobnému spuštění

		String selectedEvent;
		do {
			selectedEvent = eventList.get(random.nextInt(eventList.size()));
		} while (selectedEvent.equals(lastEvent));

		lastEvent = selectedEvent;
		Bukkit.broadcastMessage(ChatColor.GOLD + "⚡ A new event is starting: " + selectedEvent + "!");

		switch (selectedEvent) {
			case "ArrowStorm":
				if (!isEventActive("ArrowStorm") && plugin.getEvents().getConfig().getBoolean("events.arrow-storm.enabled", true)) {
					setCurrentEvent(new ArrowStormEvent(plugin, safeZoneManager, arenaManager));
				}
				break;
			case "NoKnockBackStick":
				if (!isEventActive("NoKnockBackStick") && plugin.getEvents().getConfig().getBoolean("events.no-knockback-stick.enabled", true)) {
					setCurrentEvent(new NoKnockBackStickEvent(plugin, knockBackStickItem, safeZoneManager));
				}
				break;
			case "LowGravity":
				if (!isEventActive("LowGravity") && plugin.getEvents().getConfig().getBoolean("events.low-gravity.enabled", true)) {
					setCurrentEvent(new LowGravityEvent(plugin, safeZoneManager));
				}
				break;
			case "ExtraPunchBow":
				if (!isEventActive("ExtraPunchBow") && plugin.getEvents().getConfig().getBoolean("events.extra-punch-bow.enabled", true)) {
					setCurrentEvent(new ExtraPunchBowEvent(plugin, safeZoneManager));
				}
				break;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				endCurrentEvent();
			}
		}.runTaskLater(plugin, eventDuration * 20L);
	}


	public void setCurrentEvent(Event event) {
		this.currentEvent = event;
	}

	public boolean isEventActive() {
		return currentEvent != null;
	}

	public boolean isEventActive(String eventName) {
		if (currentEvent == null) return false;

		if (currentEvent instanceof NoKnockBackStickEvent && eventName.equalsIgnoreCase("NoKnockBackStick")) {
			return true;
		}
		if (currentEvent instanceof ArrowStormEvent && eventName.equalsIgnoreCase("ArrowStorm")) {
			return true;
		}
		if (currentEvent instanceof LowGravityEvent && eventName.equalsIgnoreCase("LowGravity")) {
			return true;
		}
		if (currentEvent instanceof ExtraPunchBowEvent && eventName.equalsIgnoreCase("ExtraPunchBow")) {
			return true;
		}

		return false;
	}

	public void endCurrentEvent() {
		if (currentEvent != null) {
			Bukkit.broadcastMessage(ChatColor.GREEN + "✅ The event has ended!");
			currentEvent = null;
			activePlayerEvents.clear(); // ✅ Vyčištění všech hráčů z aktivních eventů
		}

		// 🛠 Ujistíme se, že další event začne
		Bukkit.getScheduler().runTaskLater(plugin, this::startRandomEvent, 20L * 10);
	}


	public void addPlayerToEvent(UUID playerId, String eventName) {
		if (!activePlayerEvents.containsKey(playerId)) {
			activePlayerEvents.put(playerId, new HashSet<>());
		}

		if (!activePlayerEvents.get(playerId).contains(eventName)) {
			activePlayerEvents.get(playerId).add(eventName);
			Player player = Bukkit.getPlayer(playerId);
			Event current = getCurrentEvent();

			if (player != null) {
				if (current instanceof NoKnockBackStickEvent && eventName.equalsIgnoreCase("NoKnockBackStick")) {
					((NoKnockBackStickEvent) current).removeKnockBackStick(player);
				} else if (current instanceof ExtraPunchBowEvent && eventName.equalsIgnoreCase("ExtraPunchBow")) {
					((ExtraPunchBowEvent) current).giveExtraPunchBow(player);
				} else if (current instanceof LowGravityEvent && eventName.equalsIgnoreCase("LowGravity")) {
					((LowGravityEvent) current).applyGravityEffect(player);
				}
			}
		}
	}


	public void applyEventEffect(Player player, String eventName) {
		UUID playerId = player.getUniqueId();

		// 🚨 **Zabráníme opakované aplikaci efektu**
		if (hasPlayerEvent(playerId, eventName)) return;

		Event current = getCurrentEvent();
		if (current == null) return;

		switch (eventName) {
			case "NoKnockBackStick":
				if (current instanceof NoKnockBackStickEvent) {
					((NoKnockBackStickEvent) current).removeKnockBackStick(player);
				}
				break;
			case "ExtraPunchBow":
				if (current instanceof ExtraPunchBowEvent) {
					((ExtraPunchBowEvent) current).giveExtraPunchBow(player);
				}
				break;
			case "LowGravity":
				if (current instanceof LowGravityEvent) {
					((LowGravityEvent) current).applyGravityEffect(player);
				}
				break;
		}

		// 🛠 **Přidáme hráče do seznamu aktivních eventů**
		addPlayerToEvent(playerId, eventName);
	}

	public void applyExistingEventEffects(Player player) {
		UUID playerId = player.getUniqueId();
		Event current = getCurrentEvent();

		if (current == null) return;

		// ❗ Hráč už event měl, ale byl eliminován → resetujeme mu účast a znovu ho přidáme
		if (!hasPlayerEvent(playerId, "LowGravity") && current instanceof LowGravityEvent) {
			applyEventEffect(player, "LowGravity");
		}
		if (!hasPlayerEvent(playerId, "ExtraPunchBow") && current instanceof ExtraPunchBowEvent) {
			applyEventEffect(player, "ExtraPunchBow");
		}
		if (!hasPlayerEvent(playerId, "NoKnockBackStick") && current instanceof NoKnockBackStickEvent) {
			applyEventEffect(player, "NoKnockBackStick");
		}
	}



	public boolean hasPlayerEvent(UUID playerId, String eventName) {
		if (!activePlayerEvents.containsKey(playerId)) {
			return false;
		}
		return activePlayerEvents.get(playerId).contains(eventName);
	}


	public void removePlayerFromEvent(UUID playerId, String eventName) {
		if (activePlayerEvents.containsKey(playerId)) {
			activePlayerEvents.get(playerId).remove(eventName);
			if (activePlayerEvents.get(playerId).isEmpty()) {
				activePlayerEvents.remove(playerId);
			}
		}
	}

	// 📌 Uloží hráče do seznamu pro vrácení KnockBack Sticku po opuštění safezóny
	public void markKnockBackStickForReturn(UUID playerId) {
		knockBackStickReturnList.add(playerId);
	}

	// 📌 Vrátí true, pokud máme hráči vrátit KnockBack Stick
	public boolean shouldReturnKnockBackStick(UUID playerId) {
		return knockBackStickReturnList.contains(playerId);
	}

	// 📌 Odebere hráče ze seznamu po vrácení KnockBack Sticku
	public void removeKnockBackStickReturn(UUID playerId) {
		knockBackStickReturnList.remove(playerId);
	}

	// ✅ Přidá hráče do seznamu pro navrácení Extra Punch Bow
	public void markExtraPunchBowReturn(UUID playerId) {
		playersToReturnExtraPunchBow.add(playerId);
	}

	// ✅ Zkontroluje, zda hráč má dostat Extra Punch Bow zpět
	public void scheduleExtraPunchBowReturn(UUID playerId) {
		playersToReturnExtraPunchBow.add(playerId);
	}

	public boolean shouldReturnExtraPunchBow(UUID playerId) {
		return playersToReturnExtraPunchBow.contains(playerId);
	}

	// ✅ Odebere hráče ze seznamu pro navrácení Extra Punch Bow
	public void removeExtraPunchBowReturn(UUID playerId) {
		playersToReturnExtraPunchBow.remove(playerId);
	}

	// ✅ Vrátí Extra Punch Bow hráči při opuštění safezóny
	public void restoreExtraPunchBow(Player player) {
		UUID playerId = player.getUniqueId();

		// ❌ Pokud hráč nemá být na seznamu pro vrácení, nic se nestane
		if (!playersToReturnExtraPunchBow.contains(playerId)) return;

		// ✅ Odebereme hráče ze seznamu
		playersToReturnExtraPunchBow.remove(playerId);

		// ✅ Použijeme existující metodu pro vytvoření Extra Punch Bow
		Event currentEvent = plugin.getCustomEventManager().getCurrentEvent();
		if (currentEvent instanceof ExtraPunchBowEvent) {
			ItemStack newBow = ((ExtraPunchBowEvent) currentEvent).createExtraPunchBow();
			player.getInventory().addItem(newBow);
			player.sendMessage(ChatColor.GREEN + "🏹 Extra Punch Bow byl obnoven!");
		}
	}

	public void resetProcessedPlayer(UUID playerId) {
		if (currentEvent instanceof ExtraPunchBowEvent) {
			((ExtraPunchBowEvent) currentEvent).resetProcessedPlayer(playerId);
		}
	}



	public Event getCurrentEvent() {
		return currentEvent;
	}



}
