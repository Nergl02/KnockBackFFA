package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Events.ArrowStormEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.ExtraPunchBowEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.LowGravityEvent;
import cz.nerkub.NerKubKnockBackFFA.Events.NoKnockBackStickEvent;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CustomEventManager {

	private final NerKubKnockBackFFA plugin;
	private final Random random = new Random();
	private Event currentEvent = null;
	private String lastEvent = null; // Poslední spuštěný event

	private final KnockBackStickItem knockBackStickItem;

	private final boolean enableRandomEvents;
	private final int minDelay;
	private final int maxDelay;
	private final int eventDuration;
	private final List<String> eventList = Arrays.asList("ArrowStorm", "NoKnockBackStick", "LowGravity", "ExtraPunchBow");

	public CustomEventManager(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem) {
		this.plugin = plugin;
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
		if (isEventActive()) return;

		int delay = (minDelay + random.nextInt(maxDelay - minDelay + 1)) * 20; // Převod na ticky
		new BukkitRunnable() {
			@Override
			public void run() {
				startRandomEvent();
			}
		}.runTaskLater(plugin, delay);
	}

	private void startRandomEvent() {
		if (isEventActive()) return;

		String selectedEvent;
		do {
			selectedEvent = eventList.get(random.nextInt(eventList.size()));
		} while (selectedEvent.equals(lastEvent)); // Zajištění, že nebude stejný event dvakrát po sobě

		lastEvent = selectedEvent; // Aktualizace posledního eventu

		Bukkit.broadcastMessage(ChatColor.GOLD + "⚡ A new event is starting: " + selectedEvent + "!");

		switch (selectedEvent) {
			case "ArrowStorm":
				if (plugin.getEvents().getConfig().getBoolean("events.arrow-storm.enabled", true)) {
					ArrowStormEvent event = new ArrowStormEvent(plugin);
					Bukkit.getPluginManager().callEvent(event);
					setCurrentEvent(event);
				}
				break;
			case "NoKnockBackStick":
				if (plugin.getEvents().getConfig().getBoolean("events.no-knockback-stick.enabled", true)) {
					NoKnockBackStickEvent event = new NoKnockBackStickEvent(plugin, knockBackStickItem);
					Bukkit.getPluginManager().callEvent(event);
					setCurrentEvent(event);
				}
				break;
			case "LowGravity":
				if (plugin.getEvents().getConfig().getBoolean("events.low-gravity.enabled", true)) {
					LowGravityEvent event = new LowGravityEvent(plugin);
					Bukkit.getPluginManager().callEvent(event);
					setCurrentEvent(event);
				}
				break;
			case "ExtraPunchBow":
				if (plugin.getEvents().getConfig().getBoolean("events.extra-punch-bow.enabled", true)) {
					ExtraPunchBowEvent event = new ExtraPunchBowEvent(plugin);
					Bukkit.getPluginManager().callEvent(event);
					setCurrentEvent(event);
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
		if (currentEvent instanceof  ExtraPunchBowEvent && eventName.equalsIgnoreCase("ExtraPunchBow")) {
			return true;
		}

		return false;
	}

	public void endCurrentEvent() {
		if (currentEvent != null) {
			Bukkit.broadcastMessage(ChatColor.GREEN + "✅ The event has ended!");
			currentEvent = null;
		}
		startRandomEventTimer();
	}
}
