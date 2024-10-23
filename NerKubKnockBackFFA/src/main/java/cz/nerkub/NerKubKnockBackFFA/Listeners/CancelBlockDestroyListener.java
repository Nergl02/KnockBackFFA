package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CancelBlockDestroyListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public CancelBlockDestroyListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onBlockDestroy(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location blockLocation = block.getLocation();

		if (player.getGameMode() != GameMode.CREATIVE) {
			// Zobraz zprávu hráči
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getMessages().getConfig().getString("prefix") +
							plugin.getMessages().getConfig().getString("block-break")));

			// Zobraz částici pouze pro hráče
			player.spawnParticle(Particle.DUST_PLUME, blockLocation.add(0, 0.5, 0), 20);

			// Zrušení události - pokud chceš blok zničit
			event.setCancelled(true);
		}
	}

}
