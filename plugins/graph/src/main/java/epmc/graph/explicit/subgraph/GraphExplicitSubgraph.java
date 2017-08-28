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

package epmc.graph.explicit.subgraph;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.GraphExporterDOT;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

// TODO documentation

/**
 * Implicitly constructed subgraph of a given graph.
 * 
 * @author Ernst Moritz Hahn
 */
public class GraphExplicitSubgraph implements GraphExplicit {
    private final BitSet initialNodes;
    private final GraphExplicit original;
    private final int[] subToOrig;
    private final int[] origToSub;
    private final GraphExplicitProperties properties;
    private int queriedNode = -1;
    private int numSuccessors;
    private int[] successors;
    private int[] origSuccNumbers;

    public GraphExplicitSubgraph(GraphExplicit original, BitSet include) {
        assert original != null;
        assert include != null;
        initialNodes = UtilBitSet.newBitSetUnbounded();
        this.original = original;
        initialNodes.or(original.getInitialNodes());
        initialNodes.and(include);
        subToOrig = new int[include.cardinality()];
        origToSub = new int[original.getNumNodes()];
        Arrays.fill(origToSub, -1);
        int subNode = 0;
        for (int origNode = include.nextSetBit(0); origNode >= 0; origNode = include.nextSetBit(origNode + 1)) {
            subToOrig[subNode] = origNode;
            origToSub[origNode] = subNode;
            subNode++;
        }
        properties = new GraphExplicitProperties(this);
        successors = new int[1];
        origSuccNumbers = new int[1];
        for (Object property : original.getGraphProperties()) {
            properties.registerGraphProperty(property, original.getGraphProperty(property));
        }
        for (Object property : original.getNodeProperties()) {
            NodeProperty innerProperty = original.getNodeProperty(property);
            NodePropertySubgraph nodeProperty = new NodePropertySubgraph(this, innerProperty);
            properties.registerNodeProperty(property, nodeProperty);
        }
        for (Object property : original.getEdgeProperties()) {
            EdgeProperty innerProperty = original.getEdgeProperty(property);
            EdgePropertySubgraph edgeProperty = new EdgePropertySubgraph(this, innerProperty);
            properties.registerEdgeProperty(property, edgeProperty);
        }
    }

    @Override
    public int getNumNodes() {
        return subToOrig.length;
    }

    @Override
    public BitSet getInitialNodes() {
        return initialNodes;
    }

    void queryNode(int node) {
        assert node >= 0;
        assert node < subToOrig.length;
        if (queriedNode == node) {
            return;
        }
        queriedNode = node;
        int origNode = subToOrig[node];
        int numOrigSucc = original.getNumSuccessors(origNode);
        numSuccessors = 0;
        for (int succNr = 0; succNr < numOrigSucc; succNr++) {
            int origSucc = original.getSuccessorNode(origNode, succNr);
            int subSucc = origToSub[origSucc];
            if (subSucc == -1) {
                continue;
            }
            ensureSuccessorsSize();
            successors[numSuccessors] = subSucc;
            origSuccNumbers[numSuccessors] = succNr;
            numSuccessors++;
        }
    }

    int getQueriedNode() {
        return queriedNode;
    }

    @Override
    public int getNumSuccessors(int node) {
        queryNode(node);
        return numSuccessors;
    }

    @Override
    public int getSuccessorNode(int node, int successor) {
        queryNode(node);
        assert successor >= 0;
        assert successor < numSuccessors;
        return successors[successor];
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    public int origToSub(int origNode) {
        assert origNode >= 0 : origNode;
        assert origNode < origToSub.length : origNode;
        assert origToSub[origNode] >= 0 : origNode;
        return origToSub[origNode];
    }

    public int subToOrig(int subNode) {
        assert subNode >= 0;
        assert subNode < subToOrig.length;
        return subToOrig[subNode];
    }

    public int getOrigSuccNumber(int node, int succNr) {
        queryNode(node);
        assert succNr >= 0 : succNr;
        assert succNr < numSuccessors;
        return origSuccNumbers[succNr];
    }

    private void ensureSuccessorsSize() {
        if (numSuccessors < successors.length) {
            return;
        }
        int size = numSuccessors + 1;
        while (size < successors.length) {
            size *= 2;
        }
        successors = Arrays.copyOf(successors, size);
        origSuccNumbers = Arrays.copyOf(origSuccNumbers, size);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        GraphExporterDOT.export(this, stream);
        return stream.toString();
    }

    @Override
    public void close() {
    }
}
