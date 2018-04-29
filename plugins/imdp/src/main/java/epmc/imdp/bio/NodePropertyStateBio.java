package epmc.imdp.bio;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class NodePropertyStateBio implements NodeProperty {
    private final GraphExplicitSparseAlternate graph;
    private final int numStates;
    private final ValueBoolean value;

    public NodePropertyStateBio(GraphExplicitSparseAlternate graph) {
        assert graph != null;
        this.graph = graph;
        this.numStates = graph.computeNumStates();
        this.value = TypeBoolean.get().newValue();
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        value.set(node < numStates);
        return value;
    }

    @Override
    public void set(int node, Value value) {
        assert false;
    }

    @Override
    public Type getType() {
        return TypeBoolean.get();
    }

}
