package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.KillsMap;
import cz.nerkub.NerKubKnockBackFFA.Items.BuildBlockItem;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.LeatherTunicItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Listeners.DoubleJumpListener;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.SubCommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

	private final NerKubKnockBackFFA plugin;
	private final ScoreBoardManager scoreBoardManager;
	private final ShopManager shopManager;
	private final ArenaManager arenaManager;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final BuildBlockItem buildBlockItem;
	private final RankManager rankManager;
	private final InventoryManager inventoryManager;
	private final PlayerMenuManager playerMenuManager;
	private final DoubleJumpListener doubleJumpListener;

	private final KillsMap killsMap;
	private final DamagerMap damagerMap;

	private ArrayList<SubCommandManager> subCommandManagers = new ArrayList<>();

	public CommandManager(NerKubKnockBackFFA plugin, ScoreBoardManager scoreBoardManager, ShopManager shopManager, ArenaManager arenaManager, KnockBackStickItem knockBackStickItem, PunchBowItem punchBowItem, LeatherTunicItem leatherTunicItem, BuildBlockItem buildBlockItem, RankManager rankManager, InventoryManager inventoryManager, PlayerMenuManager playerMenuManager, DoubleJumpListener doubleJumpListener, KillsMap killsMap, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.scoreBoardManager = scoreBoardManager;
		this.shopManager = shopManager;
		this.arenaManager = arenaManager;
		this.knockBackStickItem = knockBackStickItem;
		this.punchBowItem = punchBowItem;
		this.leatherTunicItem = leatherTunicItem;
		this.buildBlockItem = buildBlockItem;
		this.rankManager = rankManager;
		this.inventoryManager = inventoryManager;
		this.playerMenuManager = playerMenuManager;
		this.doubleJumpListener = doubleJumpListener;
		this.killsMap = killsMap;
		this.damagerMap = damagerMap;
		subCommandManagers.add(new ShopSubCommand(plugin, shopManager));
		subCommandManagers.add(new ReloadSubCommand(plugin, scoreBoardManager, doubleJumpListener, rankManager));
		subCommandManagers.add(new SetArenaSpawnCommand(plugin));
		subCommandManagers.add(new RemoveArenaCommand(plugin));
		subCommandManagers.add(new ArenaJoinCommand(plugin, arenaManager, knockBackStickItem, punchBowItem, leatherTunicItem, buildBlockItem, scoreBoardManager, rankManager, inventoryManager, killsMap));
		subCommandManagers.add(new ArenaLeaveCommand(plugin, inventoryManager, arenaManager, scoreBoardManager, damagerMap));
		subCommandManagers.add(new ArenaListCommand(plugin));
		subCommandManagers.add(new ArenaTeleportCommand(plugin));
		subCommandManagers.add(new PlayerMenuSubCommand(plugin, playerMenuManager));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player player) {
			if (args.length > 0) {
				for (int i = 0; i < getSubCommands().size(); i++) {
					if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
						getSubCommands().get(i).perform(player, args);
					}
				}
			} else if (args.length == 0) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Visit our Discord &bhttps://discord.gg/YXm26egK6g &7for help!"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------- " + "&7[&3KnockBack&bFFA&7]" + " &7-------"));
				for (int i = 0; i < getSubCommands().size(); i++) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6> &3" + getSubCommands().get(i).getSyntax()) + ChatColor.translateAlternateColorCodes('&', " &b- ") +
							ChatColor.translateAlternateColorCodes('&', "&7" + getSubCommands().get(i).getDescription()));
				}
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7----------------------------"));

			}
		}

		return true;
	}

	public ArrayList<SubCommandManager> getSubCommands() {
		return subCommandManagers;
	}
}
