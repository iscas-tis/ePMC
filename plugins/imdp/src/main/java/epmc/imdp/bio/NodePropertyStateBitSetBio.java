package epmc.imdp.bio;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class NodePropertyStateBitSetBio implements NodeProperty {
    private final GraphExplicitSparseAlternate graph;
    private final int numStates;
    private final BitSet bitSet;
    private final ValueBoolean value;

    NodePropertyStateBitSetBio(GraphExplicitSparseAlternate graph, BitSet bitSet) {
        assert graph != null;
        assert bitSet != null;
        this.graph = graph;
        this.numStates = graph.computeNumStates();
        this.bitSet = bitSet;
        this.value = TypeBoolean.get().newValue();
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        value.set(node < numStates ? bitSet.get(node) : false);
        return value;
    }

    @Override
    public void set(int node, Value value) {
    }

    @Override
    public Type getType() {
        return TypeBoolean.get();
    }

}
