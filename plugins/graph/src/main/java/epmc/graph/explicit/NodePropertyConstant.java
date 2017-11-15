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

package epmc.graph.explicit;

import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

/**
 * Node property in which all nodes are assigned the same value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NodePropertyConstant implements NodeProperty {
    /** Graph to which this node property belongs. */
    private GraphExplicit graph;
    /** Value returned by {@link #get()} . */
    private final Value value;
    private final OperatorEvaluator set;

    /**
     * Create new constant node property.
     * None of parameters may be {@code null}.
     * 
     * @param graph graph to which the node property belongs
     * @param value value assigned to all nodes
     */
    public NodePropertyConstant(GraphExplicit graph, Value value) {
        assert graph != null;
        assert value != null;
        this.graph = graph;
        this.value = UtilValue.clone(value);
        set = ContextValue.get().getEvaluator(OperatorSet.SET, value.getType(), value.getType());
    }

    /**
     * {@inheritDoc}
     * In this implementation, the value is the same value for all nodes,
     * given by the constructor of this class.
     */
    @Override
    public Value get(int node) {
        return value;
    }

    /**
     * {@inheritDoc}
     * In this implementation, this method will set the value for all nodes
     * of the graph.
     */
    @Override
    public void set(int node, Value value) {
        assert value != null;
        set.apply(this.value, value);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }
}
