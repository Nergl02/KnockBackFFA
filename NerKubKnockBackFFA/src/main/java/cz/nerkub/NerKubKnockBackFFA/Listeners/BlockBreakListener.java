package cz.nerkub.NerKubKnockBackFFA.Listeners;


import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;


public class BlockBreakListener implements Listener {

	private NerKubKnockBackFFA plugin;

	public BlockBreakListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
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
					Material.QUARTZ_BLOCK,	  // První fáze: quartz block
					Material.GREEN_CONCRETE,  // Druhá fáze: zelený beton
					Material.YELLOW_CONCRETE, // Třetí fáze: žlutý beton
					Material.ORANGE_CONCRETE, // Čtvrtá fáze: oranžový beton
					Material.RED_CONCRETE     // Pátá fáze: červený beton
			};

			// Spustíme proces postupného měnění bloku na materiály ve stages poli
			changeBlockInStages(block, stages, 0);
		}
	}

	public void changeBlockInStages(final Block block, final Material[] stages, final int stageIndex) {
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
