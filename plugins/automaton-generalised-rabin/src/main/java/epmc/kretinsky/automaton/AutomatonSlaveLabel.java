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

import epmc.automaton.AutomatonLabelUtil;

final class AutomatonSlaveLabel implements AutomatonLabelUtil {
    private final int[] content;
    private final AutomatonMojmir observerMojmir;
    private final int input;
    private int number;

    AutomatonSlaveLabel(AutomatonSlaveState from, int input) {
        this.observerMojmir = from.getObserverMojmir();
        this.content = from.getContent();
        this.input = input;
    }

    boolean isFailOrBuy(boolean[] finalStates, int rank) {
        for (int state1Nr = 0; state1Nr < content.length; state1Nr++) {
            if (content[state1Nr] == Integer.MAX_VALUE) {
                continue;
            }
            observerMojmir.queryState(input, state1Nr);
            int succ1Nr = observerMojmir.getSuccessorState();
            // check if fail
            if (!finalStates[succ1Nr] && observerMojmir.isSink(succ1Nr)) {
                return true;
            }
            if (content[state1Nr] >= rank) {
                continue;
            }
            // buys initial state
            if (succ1Nr == 0 && !finalStates[0]) {
                return true;
            }

            // buys nothing as goes to final state
            if (finalStates[succ1Nr]) {
                continue;
            }
            for (int state2Nr = 0; state2Nr < content.length; state2Nr++) {
                // buys nothing as no mark there
                if (content[state2Nr] == Integer.MAX_VALUE) {
                    continue;
                }
                observerMojmir.queryState(input, state2Nr);
                int succ2Nr = observerMojmir.getSuccessorState();
                if (succ1Nr == succ2Nr) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isSuccess(boolean[] finalStates, int rank) {
        for (int stateNr = 0; stateNr < content.length; stateNr++) {
            if (content[stateNr] != rank) {
                continue;
            }
            observerMojmir.queryState(input, stateNr);
            int succNr = observerMojmir.getSuccessorState();
            if (finalStates[succNr]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = Arrays.hashCode(content) + (hash << 6) + (hash << 16) - hash;
        hash = input + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AutomatonSlaveLabel)) {
            return false;
        }
        AutomatonSlaveLabel other = (AutomatonSlaveLabel) obj;
        if (!Arrays.equals(this.content, other.content)) {
            return false;
        }
        if (this.input != other.input) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("slaveLabel(");
        builder.append(Arrays.toString(content));
        builder.append(",");
        builder.append(input);
        builder.append(")");
        return builder.toString();
    }
}
