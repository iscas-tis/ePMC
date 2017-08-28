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

package epmc.graph;

import epmc.graph.Semantics;

/**
 * Semantics type for interactive Markov chains (IMCs).
 */
public enum SemanticsIMC implements SemanticsContinuousTime, SemanticsNonDet, SemanticsStochastic {
    /** Singleton element. */
    IMC;

    /**
     * Checks whether this is an interactive Markov chain (IMC).
     * 
     * @return whether this is an interactive Markov chain (IMC)
     */
    public static boolean isIMC(Semantics semantics) {
        return semantics instanceof SemanticsIMC;
    }
}
