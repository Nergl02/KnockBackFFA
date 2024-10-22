package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CancelBlockDestroyListener implements Listener {

	private final NerKubKnockBackFFA plugin;

	public CancelBlockDestroyListener(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onBlockDestroy (BlockBreakEvent event) {
		event.setCancelled(true);
	}

}
