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

import epmc.graph.Scheduler;

/**
 * Implementation of a simple scheduler using Java arrays.
 * 
 * @author Ernst Moritz Hahn
 */
public final class SchedulerSimpleArray implements SchedulerSimpleSettable {
    /** Graph this scheduler belongs to. */
    private final GraphExplicit graph;
    /** Decisions for the nodes of the graph. */
    private final int[] content;

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
    public void set(int node, int decision) {
        content[node] = decision;
    }

    @Override
    public int getDecision(int node) {
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
}
