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

package epmc.automata.determinisation;

import java.util.Arrays;

import epmc.automaton.AutomatonStateBuechiSubset;
import epmc.automaton.AutomatonStateUtil;
import epmc.util.BitSet;

final class AutomatonScheweState implements AutomatonStateUtil, AutomatonStateBuechiSubset {
    private final AutomatonSchewe automaton;
    private BitSet states;
    private int acceptance;
    private AutomatonScheweState[] children;
    private int index;
    private int number;

    AutomatonScheweState(AutomatonSchewe automaton, BitSet initNodes,
            AutomatonScheweState[] children) {
        this.automaton = automaton;
        if (initNodes != null) {
            this.states = initNodes.clone();
        }
        this.acceptance = 0;
        if (children != null) {
            this.children = children;
        } else {
            this.children = new AutomatonScheweState[0];
        }
    }

    AutomatonScheweState(AutomatonSchewe automaton) {
        this(automaton, (BitSet) null, null);
    }

    AutomatonScheweState(AutomatonScheweState other) {
        this.automaton = other.automaton;
        this.states = other.states.clone();
        this.acceptance = other.acceptance;
        this.children = new AutomatonScheweState[other.children.length];
        this.index = other.index;
        for (int child = 0; child < other.children.length; child++) {
            this.children[child] = new AutomatonScheweState(other.children[child]);
        }
    }

    @Override
    public AutomatonStateUtil clone() {
        return new AutomatonScheweState(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AutomatonScheweState)) {
            return false;
        }
        AutomatonScheweState other = (AutomatonScheweState) obj;
        if (!this.states.equals(other.states)) {
            return false;
        }
        if (this.acceptance != other.acceptance) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = states.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = acceptance + (hash << 6) + (hash << 16) - hash;
        hash = index + (hash << 6) + (hash << 16) - hash;
        hash = Arrays.deepHashCode(children) + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public BitSet getStates() {
        return states;
    }

    void setStates(BitSet states) {
        this.states = states;
    }

    int getAcceptance() {
        return acceptance;
    }

    void setAcceptance(int acceptance) {
        this.acceptance = acceptance;
    }

    AutomatonScheweState[] getChildren() {
        return children;
    }

    void setChildren(AutomatonScheweState[] children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indent) {
        assert indent >= 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            builder.append("  ");
        }
        builder.append(states.toString());
        builder.append(" " + acceptance);
        if (automaton.isParity()) {
            builder.append("  " + index + "  ");
        }
        if (children.length > 0) {
            builder.append("\n");
        }
        for (AutomatonScheweState child : children) {
            builder.append(child.toString(indent + 1));
        }
        return builder.toString();
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        assert index >= 0;
        this.index = index;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }
}
