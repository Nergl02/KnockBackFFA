package cz.nerkub.NerKubKnockBackFFA.Managers;

public class PlayerStats {

	private final String uuid;
	private final String name;
	private int kills;
	private int deaths;
	private int maxKillstreak;
	private int elo;
	private int coins;
	private String rank;

	public PlayerStats(String uuid, String name, int kills, int deaths, int maxKillstreak, int elo, int coins, String rank) {
		this.uuid = uuid;
		this.name = name;
		this.kills = kills;
		this.deaths = deaths;
		this.maxKillstreak = maxKillstreak;
		this.elo = elo;
		this.coins = coins;
		this.rank = rank;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getMaxKillstreak() {
		return maxKillstreak;
	}

	public void setMaxKillstreak(int maxKillstreak) {
		this.maxKillstreak = maxKillstreak;
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		this.elo = elo;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public double getKDRatio() {
		return deaths > 0 ? (double) kills / deaths : kills;
	}
}
