package epmc.constraintsolver;

/**
 * Direction for which a given problem should be optimised.
 * 
 * @author Ernst Moritz Hahn
 */
public enum Direction {
	/** Solution is unspecified (any valid solution suffices). */
	FEASIBILITY,
	/** Minimal solution is required. */
    MIN,
    /** Maximal solution is required. */
    MAX
    ;

	/**
	 * Check whether the direction is unspecified.
	 * 
	 * @return whether the direction is unspecified
	 */
	public boolean isFeasibility() {
		return this == FEASIBILITY;
	}

	/**
	 * Check whether a maximising solution is required.
	 * 
	 * @return whether a maximising solution is required.
	 */
    public boolean isMax() {
        return this == MAX;
    }

	/**
	 * Check whether a minimising solution is required.
	 * 
	 * @return whether a minimising solution is required.
	 */
    public boolean isMin() {
        return this == MIN;
    }
}
