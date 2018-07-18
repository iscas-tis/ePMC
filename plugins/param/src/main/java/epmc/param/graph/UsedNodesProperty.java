package epmc.param.graph;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class UsedNodesProperty implements NodeProperty {
    private final MutableGraph graph;
    private final BitSet usedNodes;
    private final TypeBoolean type;
    private final ValueBoolean value;

    UsedNodesProperty(MutableGraph graph) {
        assert graph != null;
        this.graph = graph;
        usedNodes = graph.getUsedNodes();
        type = TypeBoolean.get();
        value = type.newValue();
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        value.set(usedNodes.get(node));
        return value;
    }

    @Override
    public void set(int node, Value value) {
    }

    @Override
    public Type getType() {
        return type;
    }

}
