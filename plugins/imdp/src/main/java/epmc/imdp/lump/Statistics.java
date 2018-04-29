package epmc.imdp.lump;

import com.google.common.base.MoreObjects;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;

public final class Statistics {
    private final static String NUM_STATES = "numStates";
    private final static String NUM_NONDET = "numNodet";
    private final static String NUM_FANOUT = "numFanout";

    private final int numStates;
    private final int numNondet;
    private final int numFanout;

    public Statistics(GraphExplicit graph) {
        assert graph != null;

        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        int numNodes = graph.getNumNodes();
        int numStates = 0;
        int numNondet = 0;
        int numFanout = 0;
        for (int node = 0; node < numNodes; node++) {
            if (stateProp.getBoolean(node)) {
                numStates++;
            } else {
                numNondet++;
                numFanout += graph.getNumSuccessors(node);
            }
        }
        this.numStates = numStates;
        this.numNondet = numNondet;
        this.numFanout = numFanout;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(NUM_STATES, numStates)
                .add(NUM_NONDET, numNondet)
                .add(NUM_FANOUT, numFanout)
                .toString();
    }

    public int getNumStates() {
        return numStates;
    }

    public int getNumNondet() {
        return numNondet;
    }

    public int getNumFanout() {
        return numFanout;
    }
}
