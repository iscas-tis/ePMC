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

package epmc.propertysolver.ltllazy.automata;

import epmc.automaton.AutomatonStateBuechiSubset;
import epmc.automaton.AutomatonStateUtil;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

final class AutomatonBreakpointState implements AutomatonStateUtil, AutomatonStateBuechiSubset {
    private final BitSet states;
    private final int acceptance;
    private final BitSet children;
    private int number = 0;

    AutomatonBreakpointState(BitSet states, int acceptance, BitSet children) {
        this.states = states.clone();
        this.acceptance = acceptance;
        this.children = children;
    }

    AutomatonBreakpointState(BitSet states) {
        this(states, 0, UtilBitSet.newBitSetUnbounded());
    }

    AutomatonBreakpointState(AutomatonSubsetState state) {
        this(state.getStates().clone(), 0, UtilBitSet.newBitSetUnbounded());
    }

    AutomatonBreakpointState(AutomatonBreakpointState other) {
        this(other.states, other.acceptance, other.children);
    }

    @Override
    protected AutomatonBreakpointState clone() {
        return new AutomatonBreakpointState(this);
    }

    @Override
    public String toString() {
        return states + " " + acceptance + " " + children;
    }

    boolean getState(int index) {
        return states.get(index);
    }

    int getAcceptance() {
        return acceptance;
    }

    boolean getChild(int index) {
        return children.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AutomatonBreakpointState)) {
            return false;
        }
        AutomatonBreakpointState other = (AutomatonBreakpointState) obj;
        return this.states.equals(other.states)
                && this.acceptance == other.acceptance
                && this.children.equals(other.children);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = states.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = acceptance + (hash << 6) + (hash << 16) - hash;
        hash = children.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public BitSet getStates() {
        return states;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return this.number;
    }
}
