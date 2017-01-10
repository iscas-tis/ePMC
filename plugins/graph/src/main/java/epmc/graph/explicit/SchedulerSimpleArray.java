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

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;

/**
 * Implementation of a simple scheduler using Java arrays.
 * 
 * @author Ernst Moritz Hahn
 */
public final class SchedulerSimpleArray implements SchedulerSimple {
    /** Graph this scheduler belongs to. */
    private final GraphExplicit graph;
    /** Decisions for the nodes of the graph. */
    private final int[] content;
    /** Value to get graph property value. */
    private final ValueInteger value;

    /**
     * Constructs a new array-based simple scheduler.
     * The scheduler will be constructed for the graph given in the graph
     * parameter. If the content parameter is non-{@code null}, it will be used
     * as the array storing the decisions of the scheduler. Note that the array
     * will not be cloned by the constructor. If the content parameter is
     * @{code null}, a new array will be constructed to store the decisions of
     * the scheduler. The graph parameter must not be @{code null}.
     * 
     * @param graph graph to construct scheduler for
     * @param content array to use for content of scheduler, or {@code null}
     */
    private SchedulerSimpleArray(GraphExplicit graph, int[] content) {
        this.graph = graph;
        if (content != null) {
            this.content = content;
        } else {
            this.content = new int[graph.getNumNodes()];
            Arrays.fill(this.content, -1);
        }
        this.value = TypeInteger.get(graph.getContextValue()).newValue();
    }
    
    /**
     * Construct a new simple simple scheduler.
     * The graph parameter must not be {@code null}.
     * Initially, the decisions for each node are set to
     * {@link Scheduler#UNSET}.
     * 
     * @param graph graph to construct scheduler for
     */
    public SchedulerSimpleArray(GraphExplicit graph) {
        this(graph, null);
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public void set(int node, int decision) {
        assert assertSet(node, decision);
        content[node] = decision;
    }

    /**
     * Function asserting correct call to {@link #set(int, int)}.
     * The method will throw an {@link AssertionError} if the contract of the
     * {@link #set(int, int)} method is violated and assertions are enabled.
     * Otherwise, it will return {@code true}.
     * 
     * @param node node parameter of {@link #set(int, int)}.
     * @param decision decision parameter of {@link #set(int, int)}.
     * @return {@code true} if succeeds
     */
    private boolean assertSet(int node, int decision) {
        assert node >= 0;
        assert node < content.length;
        int previousNode = graph.getQueriedNode();
        try {
            graph.queryNode(node);
        } catch (EPMCException e) {
            e.printStackTrace();
            assert false;
        }
        assert decision >= -1 && decision < graph.getNumSuccessors();
        try {
            graph.queryNode(previousNode);
        } catch (EPMCException e) {
            e.printStackTrace();
            assert false;
        }
        return true;
    }

    @Override
    public int get(int node) {
        assert node >= 0;
        assert node < content.length;
        return content[node];
    }
    
    @Override
    public String toString() {
        return Arrays.toString(content);
    }
    
    @Override
    public SchedulerSimple clone() {
        return new SchedulerSimpleArray(graph, content.clone());
    }

    @Override
    public Value get() throws EPMCException {
        value.set(get(graph.getQueriedNode()));
        return value;
    }

    @Override
    public void set(Value value) throws EPMCException {
        assert value != null;
        set(graph.getQueriedNode(), ValueInteger.asInteger(value).getInt());
    }

    @Override
    public Type getType() {
        return value.getType();
    }
}
