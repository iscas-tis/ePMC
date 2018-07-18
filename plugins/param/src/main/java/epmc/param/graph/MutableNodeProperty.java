package epmc.param.graph;

import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class MutableNodeProperty implements NodeProperty {
    private final MutableGraph graph;
    private final Type type;
    private final Value value;
    private ValueArray content;
    private final Value entry;
    int numNodes;

    MutableNodeProperty(MutableGraph graph, Type type) {
        assert graph != null;
        assert type != null;
        this.graph = graph;
        this.type = type;
        this.value = type.newValue();
        TypeArray typeArray = type.getTypeArray();
        this.content = UtilValue.newArray(typeArray, 1);
        entry = type.newValue();
    }
    
    @Override
    public MutableGraph getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        content.get(value, node);
        return value;
    }

    @Override
    public void set(int node, Value value) {
        content.set(value, node);
    }

    @Override
    public Type getType() {
        return type;
    }

    public void addNode() {
        numNodes++;
        ensureSize();        
    }

    private void ensureSize() {
        content = ensureSize(content, numNodes + 1);
    }
    
    @Override
    public void set(int node, int value) {
        ((ValueArrayAlgebra) content).set(value, node);
    }
    
    public ValueArray ensureSize(ValueArray array, int size) {
        if (size <= array.size()) {
            return array;
        }
        int newSize = 1;
        while (newSize < size) {
            newSize <<= 1;
        }
        ValueArray result = UtilValue.newArray(array.getType(), newSize);
        for (int i = 0; i < array.size(); i++) {
            array.get(entry, i);
            result.set(entry, i);
        }
        return result;
    }
}
