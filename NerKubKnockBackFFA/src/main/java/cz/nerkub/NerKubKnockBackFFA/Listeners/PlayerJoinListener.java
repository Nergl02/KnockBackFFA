package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillStreakMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Managers.ArenaManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.ScoreBoardManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
	private final ArenaManager arenaManager;
	private final ScoreBoardManager scoreBoardManager;

	private final DamagerMap damagerMap;
	private final KillStreakMap killStreakMap;

	public PlayerJoinListener(NerKubKnockBackFFA plugin, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem, ArenaManager arenaManager, ScoreBoardManager scoreBoardManager, DamagerMap damagerMap, KillStreakMap killStreakMap) {
		this.plugin = plugin;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
		this.arenaManager = arenaManager;
		this.scoreBoardManager = scoreBoardManager;
		this.damagerMap = damagerMap;
		this.killStreakMap = killStreakMap;
	}

	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (plugin.getConfig().getBoolean("bungee-mode")) {
			// TODO Teleportovat hráče na základě aktivní arény!!
			arenaManager.teleportPlayerToCurrentArena(player);
		}

		plugin.getScoreBoardManager().startScoreboardUpdater(player);

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
		Player player = event.getPlayer();
		// TODO
		// if in config.yml leave-message set to true, take leave-message from messages.yml if false, set to null
		killStreakMap.removeInt(player.getUniqueId());
		damagerMap.removeDamager(player.getUniqueId());
		event.setQuitMessage(null);
	}
}
