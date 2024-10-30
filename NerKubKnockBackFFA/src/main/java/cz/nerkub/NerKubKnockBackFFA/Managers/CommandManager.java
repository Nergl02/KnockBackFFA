package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import cz.nerkub.NerKubKnockBackFFA.SubCommands.ReloadSubCommand;
import cz.nerkub.NerKubKnockBackFFA.SubCommands.SetArenaSpawnCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

	private final NerKubKnockBackFFA plugin;
	private final ScoreBoardManager scoreBoardManager;

	private ArrayList<SubCommandManager> subCommandManagers = new ArrayList<>();

	public CommandManager(NerKubKnockBackFFA plugin, ScoreBoardManager scoreBoardManager) {
		this.plugin = plugin;
		this.scoreBoardManager = scoreBoardManager;
		subCommandManagers.add(new ReloadSubCommand(NerKubKnockBackFFA.getPlugin(), scoreBoardManager));
		subCommandManagers.add(new SetArenaSpawnCommand(NerKubKnockBackFFA.getPlugin()));
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
