package epmc.rddl.options;

public final class RDDLIntRange {
	private final int lower;
	private final int upper;

	public RDDLIntRange(int lower, int upper) {
		assert lower <= upper;
		this.lower = lower;
		this.upper = upper;
	}
	
	public int getLower() {
		return this.lower;
	}
	
	public int getUpper() {
		return this.upper;
	}
}
