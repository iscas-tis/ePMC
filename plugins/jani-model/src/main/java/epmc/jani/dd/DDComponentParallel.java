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

package epmc.jani.dd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.jani.model.Action;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentParallel;

/**
 * DD-based symbolic representation of a {@link ComponentParallel}.
 * 
 * @author Ernst Moritz Hahn
 */
final class DDComponentParallel implements DDComponent {
    /** Whether DD component was already closed and cannot be used further. */
    private boolean closed;
    /** Graph to which this DD component belongs. */
    private GraphDDJANI graph;
    /** Component which this DD component represents. */
    private ComponentParallel component;
    /** Symbolic representation of left part of the parallel composition. */
    private DDComponent left;
    /** Symbolic representation of right part of the parallel composition. */
    private DDComponent right;
    /** List of symbolic transitions from edges of the parallel composition. */
    private final List<DDTransition> transitions = new ArrayList<>();
    /** Unmodifiable symbolic transitions list from edges of parallel composition. */
    private final List<DDTransition> transitionsExternal = Collections.unmodifiableList(transitions);
    /** Initial nodes of the parallel composition. */
    private DD initialNodes;
    /** Synchronising actions of the DD component. */
    private final Set<Action> synchronisingActions = new LinkedHashSet<>();
    /** Present state cube of the component. */
    private DD presCube;
    /** Next state cube of the component. */
    private DD nextCube;
    /** List of local variable DDs and automaton location variables. */
    private Set<VariableDD> localVariableDDs = new LinkedHashSet<>();
    /** Unmodifiable list of local variable DDs and automaton location variables. */
    private Set<VariableDD> localVariableDDsExternal = Collections.unmodifiableSet(localVariableDDs);

    @Override
    public void setGraph(GraphDDJANI graph) {
        assert this.graph == null;
        assert graph != null;
        this.graph = graph;
    }

    @Override
    public void setComponent(Component component) {
        assert this.component == null;
        assert component != null;
        assert component instanceof ComponentParallel;
        this.component = (ComponentParallel) component;
    }

    @Override
    public void build() {
        assert graph != null;
        assert component != null;
        PreparatorDDComponent preparator = new PreparatorDDComponent();
        left = preparator.prepare(graph, component.getLeft());
        right = preparator.prepare(graph, component.getRight());
        initialNodes = left.getInitialNodes().and(right.getInitialNodes());
        synchronisingActions.addAll(component.getActions());
        buildTransitions();
        presCube = left.getPresCube().and(right.getPresCube());
        nextCube = left.getNextCube().and(right.getNextCube());
        localVariableDDs.addAll(left.getVariables());
        localVariableDDs.addAll(right.getVariables());
    }

    /**
     * Builds the transitions of the parallel composition.
     * 
     */
    private void buildTransitions() {
        for (DDTransition leftTransition : left.getTransitions()) {
            if (!synchronisingActions.contains(leftTransition.getAction())) {
                continue;
            }
            for (DDTransition rightTransition : right.getTransitions()) {
                if (leftTransition.getAction() != rightTransition.getAction()) {
                    continue;
                }
                DDTransition transition = new DDTransition();
                transition.setAction(leftTransition.getAction());
                Set<VariableDD> writes = new LinkedHashSet<>();
                writes.addAll(leftTransition.getWrites());
                writes.addAll(rightTransition.getWrites());
                transition.setWrites(writes);
                DD guard = leftTransition.getGuard().and(rightTransition.getGuard());
                transition.setGuard(guard);
                DD transitionDD = leftTransition.getTransitions().multiply(rightTransition.getTransitions());
                transition.setTransitions(transitionDD);
                boolean disjoint = Collections.disjoint(leftTransition.getWrites(), rightTransition.getWrites());
                transition.setInvalid(!disjoint);
                if (guard.isFalse()) {
                    transition.close();
                } else {
                    transitions.add(transition);
                }
                Set<VariableValid> valid = new LinkedHashSet<>();
                for (VariableValid validVar : leftTransition.getValidFor()) {
                    valid.add(validVar.clone());
                }
                for (VariableValid validVar : rightTransition.getValidFor()) {
                    valid.add(validVar.clone());
                }
                transition.setVariableValid(valid);
            }
        }
        for (DDTransition leftTransition : left.getTransitions()) {
            if (synchronisingActions.contains(leftTransition.getAction())) {
                continue;
            }
            transitions.add(leftTransition.clone());
        }

        for (DDTransition rightTransition : right.getTransitions()) {
            if (synchronisingActions.contains(rightTransition.getAction())) {
                continue;
            }
            transitions.add(rightTransition.clone());
        }
    }

    @Override
    public List<DDTransition> getTransitions() {
        return transitionsExternal;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        left.close();
        right.close();
        for (DDTransition transition : transitions) {
            transition.close();
        }
        presCube.dispose();
        nextCube.dispose();
        initialNodes.dispose();		
    }

    @Override
    public DD getInitialNodes() {
        return initialNodes;
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public Set<VariableDD> getVariables() {
        return localVariableDDsExternal;
    }
}
