package cz.nerkub.NerKubKnockBackFFA.SubCommands;

import cz.nerkub.NerKubKnockBackFFA.Managers.PlayerMenuManager;
import cz.nerkub.NerKubKnockBackFFA.Managers.SubCommandManager;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.entity.Player;

public class PlayerMenuSubCommand extends SubCommandManager {

	private final NerKubKnockBackFFA plugin;
	private final PlayerMenuManager playerMenuManager;

	public PlayerMenuSubCommand(NerKubKnockBackFFA plugin, PlayerMenuManager playerMenuManager) {
		this.plugin = plugin;
		this.playerMenuManager = playerMenuManager;
	}

	@Override
	public String getName() {
		return "menu";
	}

	@Override
	public String getDescription() {
		return "&7Opens a menu for player";
	}

	@Override
	public String getSyntax() {
		return "&d/knbffa menu";
	}

	@Override
	public boolean perform(Player player, String[] args) {

		playerMenuManager.openMenu(player);

		return false;
	}
}
