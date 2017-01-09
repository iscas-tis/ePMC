package epmc.coalition.options;

/**
 * Class collection options used for coalition solver parity games plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsCoalition {
	/** Base name of resource file for options description. */
	OPTIONS_COALITION,
	COALITION_CATEGORY,
	/** Solver to use to solve stochastic parity games. */
	COALITION_SOLVER,
	/** Solver to use to solve non-stochastic parity games. */
	COALITION_SOLVER_NON_STOCHASTIC,
	/** Use shortcut for subgames where colors either all even or all odd. */
	COALITION_SAME_COLOR_SHORTCUT,
	/** Method for selecting nodes to be lifted. */
	COALITION_JURDZINSKY_CHOOSE_LIFT_NODES,
	/** Order in which nodes are lifted. */
	COALITION_JURDZINSKY_LIFT_ORDER,
	/** Silence solvers called by Schewe solver for quantitative games to
	 * prevent excessive output. */
	COALITION_QUANTITATIVE_SCHEWE_SILENCE_INTERNAL,
	/** Tolerance to decide when in qua*/
	COALITION_QUANTITATIVE_SCHEWE_COMPARE_TOLERANCE,
	;
	/**
	 * Mechanism to select nodes to be lifted in Jurdzinsky's algorithm.
	 * 
	 * @author Ernst Moritz Hahn
	 */
	public enum JurdzinskyChooseLiftNodes {
		/** Lift only nodes a successor of which changed.
		 * Might be faster than always trying to lift all nodes in one
		 * iteration, but requires computation of predecessors.
		 * */
		SUCCESSOR_CHANCED,
		/** Always try to lift all nodes in one iteration round. */
		ALL
	}
	
	public enum JurdzinskyLiftOrder {
		/** Handle nodes which are added first last. */
		LIFO,
		/** Handle nodes which are added first first. */
		FIFO
	}
	
	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private OptionsCoalition() {
	}
}
