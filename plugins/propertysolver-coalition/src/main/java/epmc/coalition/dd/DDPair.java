package epmc.coalition.dd;

import com.google.common.base.MoreObjects;

import epmc.dd.DD;

/**
 * Stores the set of winning nodes for player 0 and 1.
 * 
 * @author Ernst Moritz Hahn
 */
final class DDPair {
	/** String containing "set0", for {@link #toString()}. */
	private final static String SET0 = "set0";
	/** String containing "set1", for {@link #toString()}. */
	private final static String SET1 = "set1";

	/** Winning nodes of player 0. */
    private final DD set0;
    /** Winning nodes of player 1. */
    private final DD set1;
    
    /**
     * Construct new winning set pair.
     * The winning set parameters may not be {@code null} and must be mutually
     * exclusive.
     * 
     * @param set0 winning set of player 0
     * @param set1 winning set of player 1
     */
    DDPair(DD set0, DD set1) {
    	assert set0 != null;
    	assert set1 != null;
        this.set0 = set0;
        this.set1 = set1;
    }
    
    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this)
    	.add(SET0, set0)
    	.add(SET1, set1)
    	.toString();
    }
    
    /**
     * Get set of winning nodes of player 0.
     * 
     * @return set of winning nodes of player 0
     */
    DD getSet0() {
        return set0;
    }
    
    /**
     * Get set of winning nodes of player 1.
     * 
     * @return set of winning nodes of player 1
     */
    DD getSet1() {
        return set1;
    }
}
