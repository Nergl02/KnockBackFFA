package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillsMap {

	public final Map<UUID, Integer> killsMap = new HashMap<>();

	public Integer putInt(UUID player) {
		return killsMap.put(player, killsMap.getOrDefault(player,0) + 1);
	}

	public Integer getInt(UUID player) {
		return killsMap.get(player);
	}

}
