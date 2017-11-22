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

package epmc.modelchecker;

import java.io.Serializable;
import java.math.BigInteger;

public class ExploreStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private final BigInteger numNodes;
    private final BigInteger numStates;
    private final BigInteger numTransitions;

    public ExploreStatistics(BigInteger numNodes, BigInteger numStates,
            BigInteger numTransitions) {
        assert numNodes != null;
        assert numStates != null;
        assert numTransitions != null;
        assert numNodes.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numStates.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numTransitions.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numStates.compareTo(numNodes) <= 0;
        this.numNodes = numNodes;
        this.numStates = numStates;
        this.numTransitions = numTransitions;
    }

    public ExploreStatistics(int numNodes, int numStates, int numTransitions) {
        this(BigInteger.valueOf(numNodes), BigInteger.valueOf(numStates),
                BigInteger.valueOf(numTransitions));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("numNodes: " + numNodes + "\n");
        builder.append("numStates: " + numStates + "\n");
        builder.append("numTransitions: " + numTransitions + "\n");
        return builder.toString();
    }

    public BigInteger getNumNodes() {
        return numNodes;
    }

    public BigInteger getNumStates() {
        return numStates;
    }

    public BigInteger getNumTransitions() {
        return numTransitions;
    }
}
