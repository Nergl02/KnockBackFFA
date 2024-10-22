package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;


public class PlayerJoinListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final BuildBlockItem buildBlockItem;

	public PlayerJoinListener(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
	}

	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Location spawn = player.getWorld().getSpawnLocation();

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			player.teleport(spawn);
		}

		player.getInventory().clear();
		player.getInventory().setItem(0, knockBackStickItem.createKnockBackStickItem());
		player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
		player.getInventory().setItem(2, punchBowItem.createBowItem());
		player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
		player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
		player.getInventory().setItem(8, buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount")));

		// TODO
		// if in config.yml join-message set to true, take join-message from messages.yml if false, set to null
		event.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
		// TODO
		// if in config.yml leave-message set to true, take leave-message from messages.yml if false, set to null
		event.setQuitMessage(null);
	}
}
