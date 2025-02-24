package cz.nerkub.NerKubKnockBackFFA;

import org.bukkit.Location;

public class Arena {

	private final String name;
	private Location spawn;
	private final Location minBounds;
	private final Location maxBounds;

	public Arena(String name, Location spawn, Location minBounds, Location maxBounds) {
		this.name = name;
		this.spawn = spawn;
		this.minBounds = minBounds;
		this.maxBounds = maxBounds;
	}

	// Název arény
	public String getName() {
		return name;
	}

	// Spawn arény
	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	// Hranice arény
	public Location getMinBounds() {
		return minBounds;
	}

	public Location getMaxBounds() {
		return maxBounds;
	}

	// Informace o aréně
	@Override
	public String toString() {
		return "Arena{" +
				"name='" + name + '\'' +
				", spawn=" + spawn +
				", minBounds=" + minBounds +
				", maxBounds=" + maxBounds +
				'}';
	}
}