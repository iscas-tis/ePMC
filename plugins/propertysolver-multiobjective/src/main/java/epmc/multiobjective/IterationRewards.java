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

package epmc.multiobjective;

import java.util.Set;

import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;

final class IterationRewards {
    private final GraphExplicitSparseAlternate graph;
    private final int numProperties;
    private final BitSet combinations;
    private final int[] combinationsFromTo;
    private final ValueArrayAlgebra[] rewards;
    private int currentState;
    private int currentCombination;

    IterationRewards(GraphExplicitSparseAlternate graph, int numProperties) {
        assert graph != null;
        assert numProperties >= 0;
        this.combinations = UtilBitSet.newBitSetUnbounded();
        this.graph = graph;
        int numStates = graph.computeNumStates();
        this.numProperties = numProperties;
        this.combinationsFromTo = new int[numStates + 1];
        this.rewards = new ValueArrayAlgebra[numProperties];
        for (int propNr = 0; propNr < numProperties; propNr++) {
            this.rewards[propNr] = newValueArrayWeight(graph.getNumNondet());
        }
    }

    int getNumStates() {
        return combinationsFromTo.length - 1;
    }

    int getNumObjectives() {
        return numProperties;
    }

    int getNumNondet() {
        return graph.getNumNondet();
    }

    int getNumEntries(int state) {
        assert state >= 0;
        assert state < combinationsFromTo.length;
        return combinationsFromTo[state + 1] - combinationsFromTo[state];
    }

    boolean get(int state, int number, int objective) {
        assert state >= 0;
        assert state < combinationsFromTo.length;
        assert number >= 0;
        assert combinationsFromTo[state] + number < combinationsFromTo[state + 1];
        assert objective >= 0;
        assert objective < numProperties;
        return combinations.get((combinationsFromTo[state] + number) * numProperties + objective);
    }

    void getReward(Value reward, int state, int succ, int objective) {
        int index = graph.getStateBounds().getInt(state) + succ;
        this.rewards[objective].get(reward, index);
    }

    ValueArrayAlgebra getRewards(int obj) {
        assert obj >= 0;
        assert obj < numProperties;
        return rewards[obj];
    }

    void addCombination(BitSet combination) {
        assert currentState < graph.computeNumStates();
        assert combination != null;
        assert combination.length() <= numProperties;
        for (int bit = 0; bit < numProperties; bit++) {
            combinations.set(currentCombination * numProperties + bit, combination.get(bit));
        }
        currentCombination++;
    }

    void setReward(Value reward, int succ, int objective) {
        assert currentState < graph.computeNumStates();
        assert reward != null;
        int index = graph.getStateBounds().getInt(currentState) + succ;
        this.rewards[objective].set(reward, index);
    }

    void finishState() {
        assert currentState < graph.computeNumStates();
        combinationsFromTo[currentState + 1] = currentCombination;
        currentState++;
    }

    void setStateCombinations(Set<BitSet> combinations) {
        for (BitSet set : combinations) {
            addCombination(set);
        }
        finishState();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int state = 0; state < combinationsFromTo.length - 1; state++) {
            builder.append(state);
            builder.append(":{");
            for (int entry = 0; entry < getNumEntries(state); entry++) {
                BitSet entrySet = UtilBitSet.newBitSetUnbounded();
                for (int obj = 0; obj < numProperties; obj++) {
                    entrySet.set(obj, get(state, entry, obj));
                }
                builder.append(entrySet);
                if (entry < getNumEntries(state) - 1) {
                    builder.append(", ");
                }
            }
            builder.append("}");
            if (state < combinationsFromTo.length - 2) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private ValueArrayAlgebra newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }
}
