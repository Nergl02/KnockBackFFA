package cz.nerkub.NerKubKnockBackFFA.Managers;

public class Rank {

	private final String name;
	private final String display;
	private final int min;
	private final int max;


	public Rank(String name, String display, int min, int max) {
		this.name = name;
		this.display = display;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return name;
	}

	public String getDisplay() {
		return display;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
}
