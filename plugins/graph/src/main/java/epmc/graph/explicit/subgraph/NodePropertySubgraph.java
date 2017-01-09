package epmc.graph.explicit.subgraph;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.Value;

public class NodePropertySubgraph implements NodeProperty {
    private final GraphExplicitSubgraph graph;
    private final NodeProperty inner;

    NodePropertySubgraph(GraphExplicitSubgraph graph, NodeProperty inner) {
        assert graph != null;
        assert inner != null;
        this.graph = graph;
        this.inner = inner;
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get() throws EPMCException {
        return inner.get();
    }

    @Override
    public void set(Value value) throws EPMCException {
        inner.set(value);
    }

    @Override
    public Type getType() {
        return inner.getType();
    }
}
