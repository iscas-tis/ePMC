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

package epmc.automaton.hoa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.automaton.BuechiTransition;
import epmc.automaton.BuechiTransitionImpl;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import static epmc.error.UtilError.ensure;

final class GraphPreparator {
    private final static class Transition {
        private final int to;
        private final Expression guard;
        private final BitSet label;

        private Transition(int to, Expression guard, BitSet label) {
            this.to = to;
            this.guard = guard;
            this.label = label;
        }

        private int getTo() {
            return to;
        }

        private Expression getGuard() {
            return guard;
        }

        private BitSet getLabel() {
            return label;
        }
    }

    private final ArrayList<String> stateNames = new ArrayList<>();
    private final HanoiHeader header;
    private final List<Set<Transition>> transitions;

    GraphPreparator(HanoiHeader header) {
        assert header != null;
        this.header = header;
        transitions = new ArrayList<>();
        for (int i = 0; i < header.getNumStates(); i++) {
            transitions.add(null);
        }
    }

    void addTransition(int from, int to, Expression guard, BitSet label) {
        assert from >= 0;
        while (transitions.size() <= from) {
            transitions.add(null);
        }
        if (transitions.get(from) == null) {
            transitions.set(from, new LinkedHashSet<>());
        }
        if (label == null) {
            label = new BitSetUnboundedLongArray();
        }
        Set<Transition> stateTransitions = transitions.get(from);
        stateTransitions.add(new Transition(to, guard, label));
    }

    HanoiHeader getHeader() {
        return header;
    }
    
    void prepareState(int from) {
        assert from >= 0;
        while (transitions.size() <= from) {
            transitions.add(null);
        }
        if (transitions.get(from) == null) {
            transitions.set(from, new LinkedHashSet<>());
        }
    }
    
    void setStateName(int state, String name) {
        while (stateNames.size() <= state) {
            stateNames.add(null);
        }
        stateNames.set(state, name);
    }
    
    void ensureStates() {
        int numStates = header.getNumStates();
        if (numStates >= 0) {
            ensure(transitions.size() >= numStates,
                    ProblemsHoa.HOA_NUM_STATES_DECLARED_STATE_MISSING,
                    transitions.size(), header.getNumStates());
            for (int state = 0; state < numStates; state++) {
                ensure(transitions.get(state) != null,
                        ProblemsHoa.HOA_NUM_STATES_DECLARED_STATE_MISSING,
                        state, header.getNumStates());
            }
        } else {
            numStates = transitions.size();
            for (int state = 0; state < numStates; state++) {
                ensure(transitions.get(state) != null,
                        ProblemsHoa.HOA_NUM_STATES_UNDECLARED_STATE_GAP,
                        state, numStates - 1);
            }
            for (int state = 0; state < numStates; state++) {
                Set<Transition> stateTransitions = transitions.get(state);
                for (Transition transition : stateTransitions) {
                    int to = transition.getTo();
                    ensure(to < numStates,
                            ProblemsHoa.HOA_NUM_STATES_UNDECLARED_INVALID_TO,
                            state, to);
                }
            }
        }
    }
    
    GraphExplicitWrapper toGraph() {
        GraphExplicitWrapper graph = new GraphExplicitWrapper();
        TypeInteger typeInteger = TypeInteger.get();
        TypeObject typeLabel = new TypeObject.Builder()
                .setClazz(BuechiTransition.class)
                .build();
        ValueInteger numLabels = typeInteger.newValue();
        graph.addSettableGraphProperty(CommonProperties.NUM_LABELS, typeInteger);
        EdgeProperty labelProp = graph.addSettableEdgeProperty(CommonProperties.AUTOMATON_LABEL, typeLabel);

        ValueObject transitionValue = typeLabel.newValue();
        for (int from = 0; from < transitions.size(); from++) {
            Set<Transition> stateTransitions = transitions.get(from);
            int numSuccessors = stateTransitions.size();
            graph.prepareNode(from, numSuccessors);
            int succNr = 0;
            for (Transition transition : stateTransitions) {
                BuechiTransition buchiTransition = new BuechiTransitionImpl(transition.getGuard(), transition.getLabel());
                graph.setSuccessorNode(from, succNr, transition.getTo());
                transitionValue.set(buchiTransition);
                labelProp.set(from, succNr, transitionValue);
                succNr++;
            }
        }

        numLabels.set(header.getNumAcc());
        BitSet init = graph.getInitialNodes();
        init.or(header.getStartStates());
        graph.setGraphProperty(CommonProperties.NUM_LABELS, numLabels);
        TypeObject typeHeader = new TypeObject.Builder()
                .setClazz(HanoiHeader.class)
                .build();
        graph.addSettableGraphProperty(HanoiHeader.class, typeHeader);
        graph.setGraphProperty(HanoiHeader.class, header);
        return graph;
    }
}
