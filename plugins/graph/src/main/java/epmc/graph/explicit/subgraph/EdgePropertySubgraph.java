package epmc.graph.explicit.subgraph;

import epmc.error.EPMCException;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.value.Type;
import epmc.value.Value;

public class EdgePropertySubgraph implements EdgeProperty {
    private final GraphExplicitSubgraph graph;
    private final EdgeProperty inner;

    public EdgePropertySubgraph(GraphExplicitSubgraph graph,
            EdgeProperty inner) {
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
    public Value get(int successor) throws EPMCException {
        return inner.get(graph.getOrigSuccNumber(successor));
    }

    @Override
    public void set(Value value, int successor) {
        inner.set(value, graph.getOrigSuccNumber(successor));
    }

    @Override
    public Type getType() {
        return inner.getType();
    }

}
