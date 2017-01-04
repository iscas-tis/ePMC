package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;

/**
 * Parity automaton label implementation used for coalition solvers.
 * 
 * @author Ernst Moritz Hahn
 */
final class SettableParityLabel implements AutomatonParityLabel {
	/** Priority of the label. */
	private final int priority;

	/**
	 * Construct new settable parity label.
	 * The priority parameter must be nonnegative.
	 * 
	 * @param priority color to set for label
	 */
	SettableParityLabel(int priority) {
		assert priority >= 0;
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
}
