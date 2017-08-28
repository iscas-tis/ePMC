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

package epmc.coalition.explicit;

import java.util.Arrays;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.util.BitSet;

final class GraphExplicitRestricted implements GraphExplicit {
    private final GraphExplicit original;
    private final BitSet restriction;
    private final int[] substitute;
    private final int maxNumSuccessors;
    private final GraphExplicitProperties properties;

    GraphExplicitRestricted(GraphExplicit original, BitSet restriction) {
        assert original != null;
        assert restriction != null;
        this.original = original;
        this.restriction = restriction;
        int numNodes = original.getNumNodes();
        int maxNumSuccessors = 0;
        for (int node = 0; node < numNodes; node++) {
            maxNumSuccessors = Math.max(maxNumSuccessors, original.getNumSuccessors(node));
        }
        this.maxNumSuccessors = maxNumSuccessors;
        substitute = new int[numNodes];
        Arrays.fill(substitute, -1);
        for (int node = 0; node < numNodes; node++) {
            int succ = restriction.nextSetBit(maxNumSuccessors * node);
            substitute[node] = succ % maxNumSuccessors;
        }
        properties = new GraphExplicitProperties(this);
        for (Object property : original.getGraphProperties()) {
            properties.registerGraphProperty(property, original.getGraphProperty(property));
        }
        for (Object property : original.getNodeProperties()) {
            properties.registerNodeProperty(property, new NodePropertyRestricted(this, original.getNodeProperty(property)));
        }
        for (Object property : original.getEdgeProperties()) {
            properties.registerEdgeProperty(property, new EdgePropertyRestricted(this, original.getEdgeProperty(property)));
        }
    }

    @Override
    public int getNumNodes() {
        return original.getNumNodes();
    }

    @Override
    public BitSet getInitialNodes() {
        return original.getInitialNodes();
    }

    @Override
    public int getNumSuccessors(int node) {
        return original.getNumSuccessors(node);
    }

    @Override
    public int getSuccessorNode(int queriedNode, int successor) {
        boolean valid = this.restriction.get(queriedNode * maxNumSuccessors + successor);
        return original.getSuccessorNode(queriedNode, valid
                ? successor
                        : substitute[queriedNode]);
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    BitSet getRestriction() {
        return restriction;
    }

    int[] getSubstitute() {
        return substitute;
    }

    int getMaxNumSuccessors() {
        return maxNumSuccessors;
    }

    public int getOrigSuccNumber(int queriedNode, int successor) {
        boolean valid = this.restriction.get(queriedNode * maxNumSuccessors + successor);
        return valid ? successor : substitute[queriedNode];
    }

    @Override
    public void close() {
    }
}
