/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.TypeWeightTransition;

/**
 * Quantitative solver for stochastic parity games.
 * Classes implementing this interface should be public and should either not
 * declare a constructor or declare a parameterless public constructor.
 * 
 * @author Ernst Moritz Hahn
 */
interface SolverQuantitative {
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
     * In addition, it must contain an edge property
     * {@link CommonProperties#WEIGHT} for the transition probabilities, which
     * must be of the same type as {@link TypeWeightTransition#get()}.
     * 
     * @param game stochastic parity game to be solved
     */
    void setGame(GraphExplicit game);

    /**
     * Set which strategies shall be computed.
     * If the function is not called, no strategies will be computed. The
     * function may not be called more than once and may only be called before
     * calling {@link #solve()}.
     * 
     * @param playerEven whether to compute strategy for player even
     * @param playerOdd whether to compute strategy for player odd
     */
    void setComputeStrategies(boolean playerEven, boolean playerOdd);

    /**
     * Solve the parity game.
     * Before calling this method, {@link #setGame(GraphExplicit)} must have
     * been called. This method must not be called more than once.
     * 
     * @return probabilities and (potentially) strategies of the game
     */
    QuantitativeResult solve();
}
