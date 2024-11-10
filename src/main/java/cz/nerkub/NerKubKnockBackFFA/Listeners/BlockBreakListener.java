package cz.nerkub.NerKubKnockBackFFA.Listeners;


import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;


public class BlockBreakListener implements Listener {

	private NerKubKnockBackFFA plugin;
	private final ArenaManager arenaManager;

	public BlockBreakListener(NerKubKnockBackFFA plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// Získání hráče, který umístil blok, a samotného bloku
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// Zkontrolujeme, zda je hráč v módu přežití (SURVIVAL) a zda umístil blok QUARTZ_BLOCK
		if (player.getGameMode() == GameMode.SURVIVAL && block.getType() == Material.QUARTZ_BLOCK) {
			// Definujeme pole s materiály, které bude blok postupně měnit
			Material[] stages = {
					Material.QUARTZ_BLOCK,      // První fáze: quartz block
					Material.GREEN_CONCRETE,  // Druhá fáze: zelený beton
					Material.YELLOW_CONCRETE, // Třetí fáze: žlutý beton
					Material.ORANGE_CONCRETE, // Čtvrtá fáze: oranžový beton
					Material.RED_CONCRETE     // Pátá fáze: červený beton
			};

			// Spustíme proces postupného měnění bloku na materiály ve stages poli
			changeBlockInStages(block, stages, 0);
		}
	}

	private boolean isInSafeZone(Location location, Location arenaSpawn) {
		int safeZoneRadius = plugin.getConfig().getInt("safe-zone-radius"); // Musí odpovídat radiusu, který jsi definoval
		return location.distance(arenaSpawn) <= safeZoneRadius;
	}

	public void changeBlockInStages(final Block block, final Material[] stages, final int stageIndex) {

		String currentArena = arenaManager.getCurrentArena();
		Location arenaSpawn = arenaManager.getArenaSpawn(currentArena);

		// Zkontroluj, zda je blok v bezpečnostní zóně
		if (isInSafeZone(block.getLocation(), arenaSpawn)) {
			return; // Pokud je v bezpečnostní zóně, nezměň blok
		}

		// Pokud je stageIndex menší než počet fází, pokračujeme v měnění materiálu
		if (stageIndex < stages.length) {
			// Nastavíme blok na aktuální materiál ze seznamu stages
			block.setType(stages[stageIndex]);

			// Naplánujeme změnu bloku na další materiál po určeném čase
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					// Rekurzivně voláme tuto metodu pro další fázi změny bloku
					changeBlockInStages(block, stages, stageIndex + 1);
				}
			}, 20 * plugin.getConfig().getLong("block-break-time"));  // Časový interval v tiku
		} else {
			// Pokud jsme prošli všechny fáze, nastavíme blok na AIR (zmizení bloku)
			block.setType(Material.AIR);
		}
	}


}
