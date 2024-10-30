package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillStreakMap {

	public final Map<UUID, Integer> killstreakMap = new HashMap<>();

	public Integer putInt(UUID player) {
		return killstreakMap.put(player, killstreakMap.getOrDefault(player, 0) + 1);
	}

	public Integer getInt(UUID player) {
		return killstreakMap.get(player);
	}

	public Integer removeInt(UUID player) {
		return killstreakMap.remove(player);
	}

}
