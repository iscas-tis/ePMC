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
import epmc.jani.model.component.ComponentRename;

/**
 * DD-based symbolic representation of a {@link ComponentRename}.
 * 
 * @author Ernst Moritz Hahn
 */
final class DDComponentRename implements DDComponent {
    /** Whether DD component was already closed and cannot be used further. */
    private boolean closed;
    /** Graph to which this DD component belongs. */
    private GraphDDJANI graph;
    /** Component which this DD component represents. */
    private ComponentRename component;
    /** Symbolic representation of renamed component of the renaming composition. */
    private DDComponent renamed;
    /** List of symbolic transitions from edges of the renaming composition. */
    private final List<DDTransition> transitions = new ArrayList<>();
    /** Unmodifiable symbolic transitions list from edges of renaming composition. */
    private final List<DDTransition> transitionsExternal = Collections.unmodifiableList(transitions);
    /** Present state cube of the component. */
    private DD presCube;
    /** Next state cube of the component. */
    private DD nextCube;
    /** Initial nodes of the renaming composition. */
    private DD initialNodes;
    /** List of local variable DDs and automaton location variables. */
    private Set<VariableDD> variableDDs = new LinkedHashSet<>();
    /** Unmodifiable list of local variable DDs and automaton location variables. */
    private Set<VariableDD> variablesExternal = Collections.unmodifiableSet(variableDDs);

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
        assert component instanceof ComponentRename;
        this.component = (ComponentRename) component;
    }

    @Override
    public void build() {
        assert graph != null;
        assert component != null;
        PreparatorDDComponent preparator = new PreparatorDDComponent();
        renamed = preparator.prepare(graph, component.getRenamed());
        for (DDTransition transitionInner : renamed.getTransitions()) {
            DDTransition transition = new DDTransition();
            Action action = transitionInner.getAction();
            Action renamed = component.getRenaming().get(action);
            if (renamed == null) {
                renamed = action;
            }
            transition.setAction(renamed);
            transition.setWrites(transitionInner.getWrites());
            transition.setGuard(transitionInner.getGuard().clone());
            transition.setInvalid(transitionInner.isInvalid());
            transition.setTransitions(transitionInner.getTransitions().clone());
            Set<VariableValid> valid = new LinkedHashSet<>();
            for (VariableValid validVar : transitionInner.getValidFor()) {
                valid.add(validVar.clone());
            }
            transition.setVariableValid(valid);
            transitions.add(transition);
        }
        presCube = renamed.getPresCube().clone();
        nextCube = renamed.getNextCube().clone();
        initialNodes = renamed.getInitialNodes().clone();
        variableDDs = renamed.getVariables();
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
        renamed.close();
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
        return variablesExternal;
    }
}
