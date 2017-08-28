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

package epmc.kretinsky.automaton;

import java.util.Arrays;
import java.util.BitSet;

import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;

final class AutomatonKretinskyProductState implements AutomatonStateUtil {
    private final AutomatonKretinskyProduct observer;
    private AutomatonStateUtil[] states;
    private int number;
    private BitSet stableKnown = new BitSet();
    private BitSet stable = new BitSet();

    AutomatonKretinskyProductState(AutomatonKretinskyProduct observer, AutomatonStateUtil[] states) {
        this.observer = observer;
        this.states = states.clone();
    }

    AutomatonKretinskyProductState(AutomatonKretinskyProductState other) {
        this(other.getAutomaton(), other.states);
    }

    @Override
    protected AutomatonStateUtil clone() {
        return new AutomatonKretinskyProductState(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof AutomatonKretinskyProductState)) {
            return false;
        }
        AutomatonKretinskyProductState other = (AutomatonKretinskyProductState) obj;
        return Arrays.equals(states, other.states);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(states);
    }

    @Override
    public String toString() {
        return Arrays.toString(states);
    }

    @Override
    public AutomatonKretinskyProduct getAutomaton() {
        return observer;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    public AutomatonStateUtil[] getStates() {
        return states;
    }

    AutomatonStateUtil getState(int number) {
        return states[number];
    }

    public AutomatonMojmirState getMaster() {
        return (AutomatonMojmirState) states[0];
    }

    public AutomatonSlaveState getSlave(int slaveNr) {
        return (AutomatonSlaveState) states[slaveNr + 1];
    }

    public boolean isStable(int pair) {
        if (this.stableKnown.get(pair)) {
            return this.stable.get(pair);
        }
        int[] acceptance = observer.getAcceptance(pair);

        DD bigAnd = observer.getContextDD().newConstant(true);
        for (int slaveNr = 0; slaveNr < acceptance.length; slaveNr++) {
            DD slaveDD = observer.getSubformulaDD(slaveNr);
            if (acceptance[slaveNr] == -1) {
                bigAnd = bigAnd.andWith(slaveDD.not());
            } else {
                AutomatonSlaveState slaveState = getSlave(slaveNr);
                DD g = slaveState.getDDAssignsRank(acceptance[slaveNr]);
                bigAnd = bigAnd.andWith(g, slaveDD.clone());
            }            
        }
        AutomatonMojmirState masterState = getMaster();
        DD masterDD = observer.expressionToDD(masterState.getExpression());
        boolean stable = bigAnd.impliesWith(masterDD.clone()).isTrue();
        stableKnown.set(pair);
        this.stable.set(pair, stable);
        return stable;
    }
}
