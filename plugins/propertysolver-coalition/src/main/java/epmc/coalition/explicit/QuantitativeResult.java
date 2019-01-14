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

import com.google.common.base.MoreObjects;

import epmc.graph.explicit.SchedulerSimple;
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
        assert ValueArray.is(probabilities);
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
