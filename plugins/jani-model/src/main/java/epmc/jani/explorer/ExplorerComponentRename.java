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

package epmc.jani.explorer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import epmc.graph.CommonProperties;
import epmc.jani.model.Action;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentRename;
import epmc.value.Value;
import epmc.value.ValueObject;

/**
 * Explorer for a rename composition component.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExplorerComponentRename implements ExplorerComponent {
    /** Explorer for which this explorer component is used. */
    private ExplorerJANI explorer;
    /** Component which this explorer is supposed to explore. */
    private ComponentRename componentRename;
    /** Explorer for component the actions of which are renamed. */
    private ExplorerComponent inner;
    /** Map from actions to renamed actions. */
    private Map<Action, Action> renaming;
    /** Label edge property of component the actions of which are renamed. */
    private PropertyEdgeAction labelInner;
    /** Label edge property. */
    private PropertyEdgeAction label;
    /** Initial nodes. */
    private Set<NodeJANI> initialNodes = new LinkedHashSet<>();
    private Component component;
    private PropertyNode state;

    @Override
    public void setExplorer(ExplorerJANI model) {
        assert this.explorer == null;
        assert model != null;
        this.explorer = model;
    }

    @Override
    public void setComponent(Component component) {
        assert component != null;
        assert component instanceof ComponentRename;
        this.component = component;
    }

    @Override
    public boolean canHandle() {
        if (!(component instanceof ComponentRename)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        assert explorer != null;
        assert component != null;
        componentRename = (ComponentRename) component;
        PreparatorComponentExplorer preparator = new PreparatorComponentExplorer();
        inner = preparator.prepare(explorer, componentRename.getRenamed());
        renaming = componentRename.getRenaming();
        labelInner = (PropertyEdgeAction) inner.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        label = new PropertyEdgeAction(explorer);
        for (NodeJANI innerNode : inner.getInitialNodes()) {
            initialNodes.add(innerNode.clone());
        }
    }

    @Override
    public void buildAfterVariables() {
        inner.buildAfterVariables();
        state = inner.getNodeProperty(CommonProperties.STATE);
    }

    @Override
    public void queryNode(NodeJANI nodeRename) {
        assert nodeRename != null;
        inner.queryNode(nodeRename);
        for (int succNr = 0; succNr < getNumSuccessors(); succNr++) {
            Action action = ValueObject.as(labelInner.get(succNr)).getObject();
            Action renamed = renaming.get(action);
            if (renamed == null) {
                renamed = action;
            }
            label.set(succNr, renamed);
        }
    }

    @Override
    public int getNumSuccessors() {
        return inner.getNumSuccessors();
    }

    @Override
    public NodeJANI getSuccessorNode(int succNr) {
        assert succNr >= 0;
        assert succNr < inner.getNumSuccessors();
        return inner.getSuccessorNode(succNr);
    }

    @Override
    public Value getGraphProperty(Object property) {
        assert property != null;
        return inner.getGraphProperty(property);
    }

    @Override
    public PropertyNode getNodeProperty(Object property) {
        assert property != null;
        return inner.getNodeProperty(property);
    }

    @Override
    public PropertyEdge getEdgeProperty(Object property) {
        assert property != null;
        if (property == CommonProperties.TRANSITION_LABEL) {
            return label;
        } else {
            return inner.getEdgeProperty(property);
        }
    }

    @Override
    public Collection<NodeJANI> getInitialNodes() {
        return initialNodes;
    }

    @Override
    public ExplorerJANI getExplorer() {
        return explorer;
    }

    @Override
    public int getNumNodeBits() {
        return inner.getNumNodeBits();
    }

    @Override
    public NodeJANI newNode() {
        return explorer.newNode();
    }

    @Override
    public void setNumSuccessors(int numSuccessors) {
        inner.setNumSuccessors(numSuccessors);
    }

    @Override
    public boolean isState(NodeJANI node) {
        return inner.isState(node);
    }

    @Override
    public boolean isState() {
        return state.getBoolean();
    }

    @Override
    public void close() {
    }
}
