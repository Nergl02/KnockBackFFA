package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SafeZoneListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public SafeZoneListener(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}


	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		String currentArena = arenaManager.getCurrentArena();
		Location arenaSpawn = arenaManager.getArenaSpawn(currentArena);

		if (currentArena == null) {
			Bukkit.getLogger().warning("Při zpracovávání události poškození nebyla nalezena žádná aktivní aréna.");
			return; // Předčasný návrat, aby se zabránilo dalšímu zpracování
		}

		// Zkontroluj, jestli je entity, která způsobila poškození, hráč nebo šíp
		if (event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			Player victim = null;

			// Zkontroluj, jestli je obětí hráč
			if (event.getEntity() instanceof Player) {
				victim = (Player) event.getEntity();
			}

			// Zkontroluj, zda je útočník v bezpečnostní zóně
			if (isInSafeZone(attacker.getLocation(), arenaSpawn)) {
				event.setCancelled(true); // Zruš útok
				attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("safe-zone.attack")));
				return; // Předčasný návrat, aby se zabránilo dalšímu zpracování
			}

			// Zkontroluj, zda je oběť v bezpečnostní zóně
			if (victim != null && isInSafeZone(victim.getLocation(), arenaSpawn)) {
				event.setCancelled(true); // Zruš útok
				attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("safe-zone.attack")));
				return; // Předčasný návrat, aby se zabránilo dalšímu zpracování
			}
		}

		// Kontrola pro šípy (pokud je damager šíp)
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player attacker = (Player) arrow.getShooter();
				Player victim = null;

				// Zkontroluj, jestli je obětí hráč
				if (event.getEntity() instanceof Player) {
					victim = (Player) event.getEntity();
				}

				// Zkontroluj, zda je útočník v bezpečnostní zóně
				if (isInSafeZone(attacker.getLocation(), arenaSpawn)) {
					event.setCancelled(true); // Zruš útok
					attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("safe-zone.attack")));
					return; // Předčasný návrat, aby se zabránilo dalšímu zpracování
				}

				// Zkontroluj, zda je oběť v bezpečnostní zóně
				if (victim != null && isInSafeZone(victim.getLocation(), arenaSpawn)) {
					event.setCancelled(true); // Zruš útok
					attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("safe-zone.attack")));
					return; // Předčasný návrat, aby se zabránilo dalšímu zpracování
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		String prefix = plugin.getMessages().getConfig().getString("prefix");
		Player player = event.getPlayer();
		Location blockLocation = event.getBlock().getLocation();

		// Získej aktuální arénu a pozici
		String currentArena = arenaManager.getCurrentArena();
		Location arenaSpawn = arenaManager.getArenaSpawn(currentArena);

		if (player.getGameMode() != GameMode.CREATIVE) {
			// Zkontroluj, zda je hráč v bezpečnostní zóně
			if (isInSafeZone(blockLocation, arenaSpawn)) {
				event.setCancelled(true); // Zruš stavění
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getMessages().getConfig().getString("safe-zone.build")));
				return; // Konec metody
			}
		}
	}

	private boolean isInSafeZone(Location location, Location arenaSpawn) {
		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius"); // Musí odpovídat radiusu, který jsi definoval
		return location.distance(arenaSpawn) <= safeZoneRadius;
	}
}
