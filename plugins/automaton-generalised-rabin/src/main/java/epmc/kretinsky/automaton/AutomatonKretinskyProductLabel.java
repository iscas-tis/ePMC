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

public final class AutomatonKretinskyProductLabel implements AutomatonGeneralisedRabinLabel, AutomatonLabelUtil {
    private final AutomatonKretinskyProduct observer;
    private int number;
    private AutomatonKretinskyProductState state;
    private AutomatonLabelUtil[] succLabel;

    public AutomatonKretinskyProductLabel(
            AutomatonKretinskyProduct observerKretinskyProduct, AutomatonKretinskyProductState current, AutomatonLabelUtil[] succLabel) {
        this.state = current;
        this.observer = observerKretinskyProduct;
        this.succLabel = succLabel;
    }

    @Override
    public boolean isAccepting(int pair, int number) {
        int[] acceptance = observer.getAcceptance(pair);
        int slaveNr = observer.acceptanceToSlaveNumber(pair, number);
        AutomatonSlaveLabel label = (AutomatonSlaveLabel) succLabel[slaveNr + 1];
        boolean[] test = observer.getStateAcc(pair, slaveNr);
        return label.isSuccess(test, acceptance[slaveNr]);
    }

    @Override
    public boolean isStable(int pair) {
        if (!state.isStable(pair)) {
            return false;
        }

        int[] acceptance = observer.getAcceptance(pair);
        int numAccepting = observer.getNumAccepting(pair);
        for (int number = 0; number < numAccepting; number++) {
            int slaveNr = observer.acceptanceToSlaveNumber(pair, number);
            AutomatonSlaveLabel label = (AutomatonSlaveLabel) succLabel[slaveNr + 1];
            boolean[] test = observer.getStateAcc(pair, slaveNr);
            if (label.isFailOrBuy(test, acceptance[slaveNr])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = Arrays.hashCode(succLabel) + (hash << 6) + (hash << 16) - hash;
        hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AutomatonKretinskyProductLabel)) {
            return false;
        }
        AutomatonKretinskyProductLabel other = (AutomatonKretinskyProductLabel) obj;
        if (!state.equals(other.state)) {
            return false;
        }
        if (!Arrays.equals(succLabel, other.succLabel)) {
            return false;
        }
        return true;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    public void toString(StringBuilder builder) {
        try {
            for (int pair = 0; pair < observer.getNumPairs(); pair++) {
                builder.append(Arrays.toString(observer.getAcceptance(pair)));
                builder.append(" : ");
                builder.append(isStable(pair) ? "S " : "U ");
                int numAccepting = observer.getNumAccepting(pair);
                for (int accNr = 0; accNr < numAccepting; accNr++) {
                    builder.append(isAccepting(pair, accNr) ? "t" : "f");
                }
                if (pair < observer.getNumPairs() - 1) {
                    builder.append(", ");
                }
            }
        } catch (Throwable e) {
            builder.append(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }
}
