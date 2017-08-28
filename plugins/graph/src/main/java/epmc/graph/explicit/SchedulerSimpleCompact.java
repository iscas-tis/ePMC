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

import epmc.graph.Scheduler;

/**
 * Space-efficient implementation of a simple scheduler using Java arrays.
 * Similar to {@link SchedulerSimpleArray}, this scheduler stores its decisions
 * in a Java array. However, in contrast to {@link SchedulerSimpleArray},
 * decisions do not necessarily use a full integer to be stored. Instead, the
 * number of bits required depends on the maximal number of successors of a node
 * of the graph. As the computations involved to access a decision for a node
 * of the graph is more complex than for {@link SchedulerSimpleArray},
 * operations might be slower.
 * 
 * @author Ernst Moritz Hahn
 */
public final class SchedulerSimpleCompact implements SchedulerSimpleSettable {
    /** String containing opening square brackets. */
    private final static String SQUARE_BRACKETS_OPEN = "[";
    /** String containing closing square brackets. */
    private final static String SQUARE_BRACKETS_CLOSE = "]";
    /** String containing a comma. */
    private final static String COMMA = ",";
    /** Log2 of {@link Long#SIZE}. */
    private static final int LOG2LONGSIZE = 6;

    /** Graph this scheduler belongs to. */
    private final GraphExplicit graph;
    /** Number of bits needed to store a scheduler decision for single node. */
    private int numEntryBits;
    /** Decisions for the nodes of the graph. */
    private final long[] content;
    private final int numNodes;

    /**
     * Constructs a new space-efficient array-based simple scheduler.
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
    private SchedulerSimpleCompact(GraphExplicit graph, long[] content) {
        this.graph = graph;
        numNodes = graph.getNumNodes();
        int numValues = 0;
        for (int node = 0; node < numNodes; node++) {
            numValues = Math.max(numValues, graph.getNumSuccessors(node));
        }
        numValues++;
        this.numEntryBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        int numBits = numEntryBits * graph.getNumNodes();
        int numLongs = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        if (content != null) {
            this.content = content;
        } else {
            this.content = new long[numLongs];
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
    public SchedulerSimpleCompact(GraphExplicit graph) {
        this(graph, null);
    }

    @Override
    public void set(int node, int decision) {
        decision++;
        for (int bitNr = 0; bitNr < numEntryBits; bitNr++) {
            boolean bitValue = (decision & (1 << bitNr)) != 0;
            int bitIndex = node * numEntryBits + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            if (bitValue) {
                content[offset] |= 1L << bitIndex;
            } else {
                content[offset] &= ~(1L << bitIndex);
            }
        }
    }

    @Override
    public int getDecision(int node) {
        assert node >= 0;
        assert node < graph.getNumNodes();
        int number = 0;
        for (int bitNr = 0; bitNr < numEntryBits; bitNr++) {
            int bitIndex = node * numEntryBits + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
        boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
        if (bitValue) {
            number |= (1 << bitNr);
        }
        }
        number--;
        return number;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(SQUARE_BRACKETS_OPEN);
        int numNodes = graph.getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            builder.append(getDecision(node));
            builder.append(COMMA);
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(SQUARE_BRACKETS_CLOSE);
        return builder.toString();
    }

    @Override
    public SchedulerSimple clone() {
        return new SchedulerSimpleCompact(graph, content.clone());
    }    
}
