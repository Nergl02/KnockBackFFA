package cz.nerkub.NerKubKnockBackFFA.TabCompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;


import java.util.ArrayList;
import java.util.List;

public class KnbffaTabCompleter implements TabCompleter{
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

			}
		}

		return suggestions;
	}
}
