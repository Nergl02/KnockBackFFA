package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillStreakMap {

	private final Map<UUID, Integer> killstreakMap = new HashMap<>();

	public int putInt(UUID player) {
		int newKillStreak = killstreakMap.getOrDefault(player, 0) + 1; // Zvýšení o 1
		killstreakMap.put(player, newKillStreak);  // Uložení nové hodnoty
		return newKillStreak;                      // Vrátí novou hodnotu
	}

	public int getInt(UUID player) {
		return killstreakMap.getOrDefault(player, 0);
	}

	public void resetKillStreak(UUID player) {
		killstreakMap.put(player, 0); // Reset na 0
	}
}


