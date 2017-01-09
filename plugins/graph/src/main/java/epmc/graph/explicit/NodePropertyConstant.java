package epmc.graph.explicit;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

/**
 * Node property in which all nodes are assigned the same value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NodePropertyConstant implements NodeProperty {
    /** Graph to which this node property belongs. */
    private GraphExplicit graph;
    /** Value returned by {@link #get()} . */
    private final Value value;
    
    /**
     * Create new constant node property.
     * None of parameters may be {@code null}.
     * 
     * @param graph graph to which the node property belongs
     * @param value value assigned to all nodes
     */
    public NodePropertyConstant(GraphExplicit graph, Value value) {
        assert graph != null;
        assert value != null;
        this.graph = graph;
        this.value = UtilValue.clone(value);
    }

    /**
     * {@inheritDoc}
     * In this implementation, the value is the same value for all nodes,
     * given by the constructor of this class.
     */
    @Override
    public Value get() {
        return value;
    }

    /**
     * {@inheritDoc}
     * In this implementation, this method will set the value for all nodes
     * of the graph.
     */
    @Override
    public void set(Value value) throws EPMCException {
        assert value != null;
        assert this.value.getType().canImport(value.getType());
        this.value.set(value);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }
}
