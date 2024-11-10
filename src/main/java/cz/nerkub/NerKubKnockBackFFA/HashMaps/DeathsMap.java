package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathsMap {

	public final Map<UUID, Integer> deathsMap = new HashMap<>();

	public Integer putInt(UUID player) {
		return deathsMap.put(player, deathsMap.getOrDefault(player, 0) + 1);
	}

	public Integer getInt(UUID player) {
		return deathsMap.get(player);
	}

}
