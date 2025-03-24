package cz.nerkub.NerKubKnockBackFFA.TabCompleters;

import cz.nerkub.NerKubKnockBackFFA.Managers.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;


import java.util.ArrayList;
import java.util.List;

public class KnbffaTabCompleter implements TabCompleter{

	private final DatabaseManager databaseManager;

	public KnbffaTabCompleter(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args) {

		List<String> suggestions = new ArrayList<>();

		if (command.getName().equalsIgnoreCase("knbffa")) {
			if (args.length == 1) {
				suggestions.add("menu");
				suggestions.add("shop");
				suggestions.add("reload");
				suggestions.add("tool");
				suggestions.add("createarena");
				suggestions.add("removearena");
				suggestions.add("setarenaspawn");
				suggestions.add("arenalist");
				suggestions.add("arenatp");
				suggestions.add("cleararrows");

			} else if (args.length == 2 && args[0].equalsIgnoreCase("removearena")) {
				suggestions.addAll(databaseManager.getAllArenaNames());
			} else  if (args.length == 2 && args[0].equalsIgnoreCase("setarenaspawn")) {
				suggestions.addAll(databaseManager.getAllArenaNames());
			} else if (args.length == 2 && args[0].equalsIgnoreCase("arenatp")) {
				suggestions.addAll(databaseManager.getAllArenaNames());
			} else if (args.length == 2 && args[0].equalsIgnoreCase("cleararrows")) {
				suggestions.addAll(databaseManager.getAllArenaNames());
			}
		}

		return suggestions;
	}
}
