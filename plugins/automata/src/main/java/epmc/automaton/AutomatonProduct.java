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

package epmc.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.expression.Expression;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class AutomatonProduct implements Automaton {
    private final class AutomatonProductLabelImpl implements AutomatonProductLabel, AutomatonLabelUtil {
        private final int[] labels;
        private int number;

        AutomatonProductLabelImpl(int[] labels) {
            this.labels = Arrays.copyOf(labels, labels.length);
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof AutomatonProductState)) {
                return false;
            }
            AutomatonProductLabelImpl other = (AutomatonProductLabelImpl) obj;
            return Arrays.equals(labels, other.labels);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(labels);
        }

        @Override
        public String toString() {
            return Arrays.toString(labels);
        }

        @Override
        public int get(int i) {
            assert i >= 0;
            assert i < labels.length;
            return labels[i];
        }

        @Override
        public int getNumber() {
            return this.number;
        }

        @Override
        public void setNumber(int number) {
            this.number = number;
        }
    }

    private final AutomatonMaps<AutomatonProductState,AutomatonProductLabelImpl> automatonMaps = new AutomatonMaps<>();

    @Override
    public int getNumStates() {
        return automatonMaps.getNumStates();
    }

    protected AutomatonProductState makeUnique(AutomatonProductState state) {
        return automatonMaps.makeUnique(state);
    }

    protected AutomatonProductLabelImpl makeUnique(AutomatonProductLabelImpl label) {
        return automatonMaps.makeUnique(label);
    }

    @Override
    public AutomatonStateUtil numberToState(int number) {
        return automatonMaps.numberToState(number);
    }

    private final static class CacheKey {
        Value[] modelState;
        int automatonState;

        CacheKey() {
        }

        CacheKey(Value[] modelState, int automatonState) {
            this.modelState = new Value[modelState.length];
            for (int i = 0; i < modelState.length; i++) {
                this.modelState[i] = UtilValue.clone(modelState[i]);
            }
            this.automatonState = automatonState;
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            if (!Arrays.equals(this.modelState, other.modelState)) {
                return false;
            }
            if (this.automatonState != other.automatonState) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = Arrays.deepHashCode(modelState) + (hash << 6) + (hash << 16) - hash;
            hash = automatonState + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(Arrays.toString(modelState));
            builder.append(",");
            builder.append(automatonState);
            builder.append(")");
            return builder.toString();
        }
    }

    private final static class CacheValue {
        AutomatonProductState state;
        AutomatonProductLabelImpl label;
    }

    public final static class AutomatonProductState implements AutomatonStateUtil {
        private final int[] states;
        private int number;

        AutomatonProductState(int[] states) {
            this.states = Arrays.copyOf(states, states.length);
        }

        AutomatonProductState(AutomatonProductState other) {
            this(other.states);
        }

        @Override
        protected AutomatonStateUtil clone() {
            return new AutomatonProductState(this);
        };

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof AutomatonProductState)) {
                return false;
            }
            AutomatonProductState other = (AutomatonProductState) obj;
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

        public int get(int i) {
            assert i >= 0;
            assert i < states.length;
            return states[i];
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

    private final List<Automaton> automataExternal;
    private final Automaton[] automata;
    private final AutomatonProductState initState;
    private AutomatonProductState succState;
    private AutomatonProductLabelImpl succLabel;
    private final int[] succStateArray;
    private final int[] succLabelingsArray;
    private final Map<CacheKey,CacheValue> cache = new HashMap<>();
    private final CacheKey testEntry = new CacheKey();

    public AutomatonProduct(Automaton[] automata) {
        assert assertConstructor(automata);
        this.automata = automata.clone();
        this.succStateArray = new int[automata.length];
        this.succLabelingsArray = new int[automata.length];
        int[] init = new int[automata.length];
        for (int i = 0; i < automata.length; i++) {
            init[i] = automata[i].getInitState();
        }
        this.initState = makeUnique(new AutomatonProductState(init));
        List<Automaton> automataList = new ArrayList<>();
        for (Automaton automaton : automata) {
            automataList.add(automaton);
        }
        automataExternal = Collections.unmodifiableList(automataList);
    }

    public AutomatonProduct(List<? extends Automaton> automata) {
        this(automata.toArray(new Automaton[0]));
    }

    private static boolean assertConstructor(Automaton[] automata) {
        assert automata != null;
        assert automata.length >= 0;
        if (automata.length == 0) {
            return true;
        }
        assert automata[0] != null;
        for (Automaton automaton : automata) {
            assert automaton != null;
        }
        return true;
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
    {
        assert assertQueryState(modelState, automatonState);
        testEntry.modelState = modelState;
        testEntry.automatonState = automatonState;
        CacheValue found = cache.get(testEntry);
        if (found != null) {
            this.succState = found.state;
            this.succLabel = found.label;
            return;
        }        
        AutomatonProductState productState = (AutomatonProductState) numberToState(automatonState);
        for (int i = 0; i < productState.states.length; i++) {
            automata[i].queryState(modelState, productState.states[i]);
            succStateArray[i] = automata[i].getSuccessorState();
            succLabelingsArray[i] = automata[i].getSuccessorLabel();
        }
        this.succState = makeUnique(new AutomatonProductState(succStateArray));
        this.succLabel = makeUnique(new AutomatonProductLabelImpl(succLabelingsArray));
        CacheKey cacheKey = new CacheKey(modelState, automatonState);
        CacheValue cacheValue = new CacheValue();
        cacheValue.state = succState;
        cacheValue.label = succLabel;
        cache.put(cacheKey, cacheValue);
    }

    private boolean assertQueryState(Value[] modelState,
            int automatonState) {
        assert automatonState >= 0;
        assert automatonState < getNumStates();
        for (Value value : modelState) {
            assert value != null;
        }
        return true;
    }

    @Override
    public int getSuccessorState() {
        return succState.getNumber();
    }

    @Override
    public int getSuccessorLabel() {
        return succLabel.getNumber();
    }

    @Override
    public Expression[] getExpressions() {
        return automata[0].getExpressions();
    }

    public int getNumComponents() {
        return automata.length;
    }

    public Automaton getAutomaton(int number) {
        assert number >= 0;
        assert number < automata.length;
        return automata[number];
    }

    public List<Automaton> getAutomata() {
        return automataExternal;
    }

    @Override
    public void close() {
        for (Automaton automaton : automata) {
            automaton.close();
        }
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return automatonMaps.numberToLabel(number);
    }
}
