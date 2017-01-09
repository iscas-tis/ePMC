package epmc.coalition.explicit;

import com.google.common.base.MoreObjects;

import epmc.graph.explicit.SchedulerSimple;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

/**
 * Result of a quantitative parity game solver.
 * 
 * @author Ernst Moritz Hahn
 */
final class QuantitativeResult {
	/** String containing "probabilities", for {@link #toString()}. */
	private final static String PROBABILITIES = "probabilities";
	/** String containing "strategies", for {@link #toString()}. */
	private final static String STRATEGIES = "strategies";

	/** Array assigning winning probability to each state of the game. */
	private ValueArrayAlgebra probabilities;
	/** Strategies (or strategy) to obtain the given value, or {@code null}. */
	private SchedulerSimple strategies;
	
	/**
	 * Construct new quantitative result.
	 * The probabilities parameter must not be {@code null}.
	 * 
	 * @param probabilities probabilities to use
	 * @param strategies strategies to use, or {@code null}
	 */
	QuantitativeResult(ValueArrayAlgebra probabilities, SchedulerSimple strategies) {
		assert probabilities != null;
		assert ValueArray.isArray(probabilities);
		assert TypeWeight.get(probabilities.getType().getContext())
				.canImport(TypeArray.asArray(probabilities.getType()).getEntryType());
		assert strategies == null ||
				probabilities.size() == strategies.getGraph().getNumNodes();
		this.probabilities = probabilities;
		this.strategies = strategies;
	}
	
	/**
	 * Get winning probabilities for even player.
	 * The result is an array of real values.
	 * 
	 * @return winning probabilities for even player
	 */
	ValueArrayAlgebra getProbabilities() {
		return probabilities;
	}
	
    /**
     * Get mutually optimal strategies of players, or {@code null}.
     * 
     * @return mutually optimal strategies of players, or {@code null}
     */
	SchedulerSimple getStrategies() {
		return strategies;
	}
	
	@Override
	public String toString() {
    	return MoreObjects.toStringHelper(this)
    	.add(PROBABILITIES, probabilities)
    	.add(STRATEGIES, strategies)
    	.toString();
	}
}