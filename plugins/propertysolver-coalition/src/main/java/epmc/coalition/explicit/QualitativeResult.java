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
import epmc.util.BitSet;

/**
 * Stores the set of winning nodes for player 0 and 1 and optionally strategies.
 * 
 * @author Ernst Moritz Hahn
 */
final class QualitativeResult {
    /** String containing "set0", for {@link #toString()}. */
    private final static String SET0 = "set0";
    /** String containing "set1", for {@link #toString()}. */
    private final static String SET1 = "set1";
    /** String containing "strategies", for {@link #toString()}. */
    private final static String STRATEGIES = "strategies";

    /** Winning nodes of player 0. */
    private final BitSet set0;
    /** Winning nodes of player 1. */
    private final BitSet set1;
    /** Optimal strategies of the two players, or {@code null}. */
    private final SchedulerSimple strategies;

    /**
     * Construct new winning set pair.
     * The winning set parameters may not be {@code null} and must be mutually
     * exclusive. The winning strategies parameter may be {@code null}.
     * 
     * @param set0 winning set of player 0
     * @param set1 winning set of player 1
     * @param strategies mutually optimal strategies, or {@code null}
     */
    QualitativeResult(BitSet set0, BitSet set1, SchedulerSimple strategies) {
        assert set0 != null;
        assert set1 != null;
        this.set0 = set0;
        this.set1 = set1;
        this.strategies = strategies;
    }

    /**
     * Construct new winning strategies pair.
     * The winning set parameters may not be {@code null} and must be mutually
     * exclusive. The winning strategies will be set to {@code null}.
     * 
     * @param set0 winning set of player 0
     * @param set1 winning set of player 1
     */
    QualitativeResult(BitSet set0, BitSet set1) {
        this(set0, set1, null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(SET0, set0)
                .add(SET1, set1)
                .add(STRATEGIES, strategies)
                .toString();
    }

    /**
     * Get set of winning nodes of player 0.
     * 
     * @return set of winning nodes of player 0
     */
    BitSet getSet0() {
        return set0;
    }

    /**
     * Get set of winning nodes of player 1.
     * 
     * @return set of winning nodes of player 1
     */
    BitSet getSet1() {
        return set1;
    }

    /**
     * Get mutually optimal strategies of players, or {@code null}.
     * 
     * @return mutually optimal strategies of players, or {@code null}
     */
    SchedulerSimple getStrategies() {
        return strategies;
    }
}
