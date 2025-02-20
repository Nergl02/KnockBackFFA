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

	public Integer getInt(UUID player) {
		return killsMap.get(player);
	}

}
