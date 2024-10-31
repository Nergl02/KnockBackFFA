package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.ScoreBoardManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadSubCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final ScoreBoardManager scoreBoardManager;

	public ReloadSubCommand(NerKubKnockBackFFA plugin, ScoreBoardManager scoreBoardManager) {
		this.plugin = plugin;
		this.scoreBoardManager = scoreBoardManager;
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
		plugin.getMessages().reloadConfig();
		plugin.getItems().reloadConfig();
		plugin.getPlayers().reloadConfig();
		plugin.getArenas().reloadConfig();
		plugin.reloadConfig();
		plugin.getShop().reloadConfig();
		scoreBoardManager.reloadScoreboard();
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getConfig().getString("prefix") + plugin.getMessages().getConfig().getString("reload")));
		return false;
	}

}
