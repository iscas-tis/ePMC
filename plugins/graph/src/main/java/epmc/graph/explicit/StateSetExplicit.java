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

package epmc.graph.explicit;

import java.io.Closeable;

import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.util.BitSet;

public final class StateSetExplicit implements Closeable, Cloneable, StateSet {
    private final BitSet statesExplicit;
    private final int[] numberToState;
    private int size;

    // note: consumes arguments statesExplicit and statesDD
    public StateSetExplicit(LowLevel lowLevel, BitSet statesExplicit) {
        assert lowLevel != null;
        if (statesExplicit == null) {
            this.statesExplicit = null;
            this.numberToState = null;
        } else {
            this.statesExplicit = statesExplicit;
            this.numberToState = new int[statesExplicit.cardinality()];
            int index = 0;
            for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
                numberToState[index] = state;
                index++;
            }
            this.size = statesExplicit.cardinality();
        }
    }

    @Override
    public StateSetExplicit clone() {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof StateSetExplicit)) {
            return false;
        }
        StateSetExplicit other = (StateSetExplicit) obj;
        if ((this.statesExplicit == null) != (other.statesExplicit == null)) {
            return false;
        }
        if (statesExplicit != null && !this.statesExplicit.equals(other.statesExplicit)) {
            return false;
        }
        return true;
    }

    public boolean isSubsetOf(StateSet states) {
        assert states != null;
        assert states instanceof StateSetExplicit;
        StateSetExplicit other = (StateSetExplicit) states;
        for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
            if (!(other.statesExplicit.get(state))) {
                return false;
            }
        }
        return true;
    }

    public int getExplicitIthState(int i) {
        assert i >= 0;
        assert i < numberToState.length;
        return numberToState[i];
    }

    public boolean isExplicitContains(int state) {
        return statesExplicit.get(state);
    }

    public int getExplicitStateNr(int explicitIthState) {
        int number = 0;
        for (int state = statesExplicit.nextSetBit(0); state >= 0; state = statesExplicit.nextSetBit(state + 1)) {
            if (number == explicitIthState) {
                return state;
            }
            number++;
        }
        assert false;
        return -1;
    }

    public BitSet getStatesExplicit() {
        return statesExplicit;
    }

    @Override
    public String toString() {
        return statesExplicit.toString();
    }
}
