package epmc.coalition.dd;

import epmc.automaton.AutomatonParityLabel;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.NodeProperty;

/**
 * Qualitative solver for stochastic parity games.
 * Classes implementing this interface should be public and should either not
 * declare a constructor or declare a parameterless public constructor.
 * 
 * @author Ernst Moritz Hahn
 */
interface SolverQualitative {
	/**
	 * Get identifier of the solver.
	 * The identifier is used to allow the user to choose between several
	 * solvers.
	 * 
	 * @return identifier of the solver
	 */
	String getIdentifier();
	
	/**
	 * Set the stochastic parity game to be solved.
	 * The game parameter must be not be {@code null}.
	 * The function must be called exactly once before calling {@link #solve()}.
	 * The game must contain a {@link NodeProperty} named
	 * {@link CommonProperties#PLAYER} of enum type {@link Player} assigning
	 * players even ({@link Player#ONE}), odd ({@link Player#TWO}, or random
	 * {@link Player#STOCHASTIC} to the
	 * nodes. Other members of {@link Player} are not valid.
	 * It must also contain a {@link NodeProperty} named
	 * {@link CommonProperties#AUTOMATON_LABEL} of object type
	 * {@link AutomatonParityLabel} assigning priorities to each node.
	 * 
	 * @param game stochastic parity game to be solved
	 */
	void setGame(GraphDD game);
	
	/**
	 * Sets whether the 
	 * @param strictEven
	 */
	void setStrictEven(boolean strictEven);
	
	void setComputeStrategies(boolean playerEven, boolean playerOdd);
	
	DDPair solve() throws EPMCException;
}
