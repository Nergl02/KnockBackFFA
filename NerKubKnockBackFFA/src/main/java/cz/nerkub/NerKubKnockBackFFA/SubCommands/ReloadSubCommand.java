package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Listeners.DoubleJumpListener;
import cz.nerkub.NerKubKnockBackFFA.Managers.RankManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.ScoreBoardManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;


public class ReloadSubCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ScoreBoardManager scoreBoardManager;
	private DoubleJumpListener doubleJumpListener;
	private final RankManager rankManager;

	public ReloadSubCommand(NerKubKnockBackFFA plugin, ScoreBoardManager scoreBoardManager, DoubleJumpListener doubleJumpListener, RankManager rankManager) {
		this.plugin = plugin;
		this.scoreBoardManager = scoreBoardManager;
		this.doubleJumpListener = doubleJumpListener;
		this.rankManager = rankManager;
	}

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "&7Reload &4all &7configs files.";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa reload";
	}

	@Override
	public boolean perform(Player player, String[] args) {

		if (!player.hasPermission("knbffa.admin")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") +
					plugin.getMessages().getConfig().getString("no-permission")));
			return false;
		}

		plugin.getMessages().reloadConfig();
		plugin.getItems().reloadConfig();
		plugin.reloadConfig();
		plugin.getShop().reloadConfig();
		plugin.getRanks().reloadConfig();
		plugin.getMenu().reloadConfig();
		rankManager.loadRanks();
		scoreBoardManager.reloadScoreboard();

		// ✅ Odregistrování starého listeneru před registrací nového
		HandlerList.unregisterAll(doubleJumpListener);

		// ✅ Vytvoření nové instance a registrace
		doubleJumpListener = new DoubleJumpListener(plugin);
		Bukkit.getPluginManager().registerEvents(doubleJumpListener, plugin);
		doubleJumpListener.reloadConfigValues();

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") + plugin.getMessages().getConfig().getString("reload")));
		return false;
	}

}
