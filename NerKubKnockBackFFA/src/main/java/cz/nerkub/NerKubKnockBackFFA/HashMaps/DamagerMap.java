package cz.nerkub.NerKubKnockBackFFA.HashMaps;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamagerMap {


	public final Map<UUID, UUID> damagerMap = new HashMap<>();


	public UUID putDamager(UUID victim, UUID damager) {
		return damagerMap.put(victim, damager);
	}

	public boolean hasDamager(UUID victim) {
		return damagerMap.containsKey(victim);
	}

	public UUID getDamager(UUID victim) {
		return damagerMap.get(victim);
	}

	public UUID removeDamager(UUID victim) {
		return damagerMap.remove(victim);
	}

}
