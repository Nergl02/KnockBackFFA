package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillsMap {

	private final NerKubKnockBackFFA plugin;

	public final Map<UUID, Integer> killsMap = new HashMap<>();

	public KillsMap(NerKubKnockBackFFA plugin) {
		this.plugin = plugin;
	}

	public Integer putInt(UUID player) {
		return killsMap.put(player, plugin.getPlayers().getConfig().getInt(player + ".kills") + 1);
	}

	public Integer getInt(UUID player) {
		return killsMap.get(player);
	}

}
