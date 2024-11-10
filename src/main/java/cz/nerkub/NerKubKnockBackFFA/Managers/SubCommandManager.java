package cz.nerkub.NerKubKnockBackFFA.Managers;

import org.bukkit.entity.Player;

public abstract class SubCommandManager {

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getSyntax();

	public abstract boolean perform(Player player, String[] args);

}
