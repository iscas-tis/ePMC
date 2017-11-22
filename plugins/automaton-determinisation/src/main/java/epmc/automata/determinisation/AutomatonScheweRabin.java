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

import epmc.automaton.AutomatonRabin;
import epmc.automaton.AutomatonSafra;
import epmc.automaton.Buechi;
import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.value.Value;

public final class AutomatonScheweRabin implements AutomatonRabin, AutomatonSafra {
    public final static class Builder implements AutomatonSafra.Builder, AutomatonRabin.Builder {
        private Buechi buechi;
        private BitSet init;

        @Override
        public Builder setBuechi(Buechi buechi) {
            this.buechi = buechi;
            return this;
        }

        private Buechi getBuechi() {
            return buechi;
        }

        @Override
        public Builder setInit(BitSet initialStates) {
            this.init = initialStates;
            return this;
        }

        private BitSet getInit() {
            return init;
        }

        @Override
        public AutomatonScheweRabin build() {
            return new AutomatonScheweRabin(this);
        }
    }

    public final static String IDENTIFIER = "schewe-rabin";

    private final AutomatonSchewe inner;

    private AutomatonScheweRabin(Builder builder) {
        AutomatonSchewe.Builder scheweBuilder = new AutomatonSchewe.Builder();
        scheweBuilder.setBuechi(builder.getBuechi());
        scheweBuilder.setInit(builder.getInit());
        scheweBuilder.setParity(false);
        this.inner = scheweBuilder.build();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public int getInitState() {
        return inner.getInitState();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
    {
        inner.queryState(modelState, automatonState);
    }

    @Override
    public int getNumStates() {
        return inner.getNumStates();
    }

    @Override
    public Object numberToState(int number) {
        return inner.numberToState(number);
    }

    @Override
    public Object numberToLabel(int number) {
        return inner.numberToLabel(number);
    }

    @Override
    public Expression[] getExpressions() {
        return inner.getExpressions();
    }

    @Override
    public int getSuccessorState() {
        return inner.getSuccessorState();
    }

    @Override
    public int getSuccessorLabel() {
        return inner.getSuccessorLabel();
    }

    @Override
    public int getNumPairs() {
        return inner.getNumPairs();
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    @Override
    public Buechi getBuechi() {
        return inner.getBuechi();
    }
}
