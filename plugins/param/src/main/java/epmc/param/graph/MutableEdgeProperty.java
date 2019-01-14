package epmc.param.graph;

import java.util.ArrayList;

import epmc.graph.explicit.EdgeProperty;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class MutableEdgeProperty implements EdgeProperty {
    private final MutableGraph graph;
    private final Type type;
    private final TypeArray typeArray;
    private final ArrayList<ValueArray> values = new ArrayList<>();
    private final IntArrayList sizes = new IntArrayList();
    private final Value value;
    private final Value tmp;

    MutableEdgeProperty(MutableGraph graph, Type type) {
        assert graph != null;
        this.graph = graph;
        this.type = type;
        typeArray = type.getTypeArray();
        value = type.newValue();
        tmp = type.newValue();
    }
    
    @Override
    public MutableGraph getGraph() {
        return graph;
    }

    @Override
    public Value get(int node, int successor) {
        assert node >= 0 : node;
        assert node < sizes.size() : node;
        assert successor >= 0 : successor;
        assert successor < sizes.getInt(node) : successor;
        values.get(node).get(value, successor);
        return value;
    }

    @Override
    public void set(int node, int successor, Value value) {
        values.get(node).set(value, successor);
    }

    @Override
    public Type getType() {
        return type;
    }

    public void addSuccessor(int node, Value value) {
        int size = sizes.getInt(node);
        ValueArray array = values.get(node);
        if (size >= array.size()) {
            int newSize = array.size();
            while (size >= newSize) {
                newSize *= 2;
            }
            
            ValueArray newArray = typeArray.newValue();
            newArray.setSize(newSize);
            for (int index = 0; index < array.size(); index++) {
                array.get(tmp, index);
                newArray.set(tmp, index);
            }
            array = newArray;
            values.set(node, array);
        }
        array.set(value, size);
        sizes.set(node, size + 1);
    }

    public void addNode() {
        ValueArray array = typeArray.newValue();
        array.setSize(1);
        values.add(array);
        sizes.add(0);
    }
    
    public void removeSuccessorNumber(int node, int succNr) {
        ValueArray nodeValues = values.get(node);
        if (graph.isRemoveShift()) {
            for (int i = succNr; i < sizes.getInt(node) - 1; i++) {
                nodeValues.get(value, i + 1);
                nodeValues.set(value, i);
            }
        } else {
            nodeValues.get(value, sizes.getInt(node) - 1);
            nodeValues.set(value, succNr);            
        }
        sizes.set(node, sizes.getInt(node) - 1);
    }

    public void clearSuccessors(int node) {
        sizes.set(node, 0);
    }
}
