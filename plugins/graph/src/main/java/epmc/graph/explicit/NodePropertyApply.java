package epmc.graph.explicit;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Node property obtaining values using the apply method of an operator.
 * The property is constructed with an {@link Operator} and a variable number of
 * {@link NodeProperty} objects, the number of which should fit the arity of the
 * operator of the edge property.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NodePropertyApply implements NodeProperty {
    /** Graph to which this node property belongs. */
    private final GraphExplicit graph;
    /** Operator computing the values obtained by {@link #get()}. */
    private final Operator operator;
    /** Node properties {@link #get()} of which {@link #operator} is applied. */
    private final NodeProperty[] operands;
    /** Values used to perform {@link Operator#apply(Value, Value...)}. */
    private final Value[] callOperands;
    /** Value returned by {@link #get()}. */
    private final Value value;
    
    /**
     * Construct a new apply node property.
     * None of the arguments may be {@code null} or contain {@code null}
     * entries.
     * 
     * @param graph graph to which this property belongs
     * @param operator operator used to generate values of {@link #get()}
     * @param operands node properties providing operands to the operator
     */
    public NodePropertyApply(GraphExplicit graph, Operator operator, NodeProperty... operands) {
        assert graph != null;
        assert operator != null;
        assert operands != null;
        for (NodeProperty nodeProperty : operands) {
            assert nodeProperty != null;
            assert nodeProperty.getGraph() == graph;
        }
        this.graph = graph;
        this.operator = operator;
        this.operands = operands;
        this.callOperands = new Value[operands.length];
        Type[] types = new Type[operands.length];
        for (int operandNr = 0; operandNr < operands.length; operandNr++) {
            types[operandNr] = operands[operandNr].getType();
        }
        Type type = operator.resultType(operator.resultType(types));
        this.value = type.newValue();
    }
    
    /**
     * {@inheritDoc}
     * For this property type, the result of this function is computed as
     * follows. For each operand, a value is obtained using {@link #get()}.
     * Afterwards, the {@link Operator#apply(Value, Value...)} method of the
     * operator of this property is used to transform these values into the
     * resulting value.
     */
    @Override
    public Value get() throws EPMCException {
        for (int operandNr = 0; operandNr < operands.length; operandNr++) {
            callOperands[operandNr] = operands[operandNr].get();
        }
        operator.apply(value, callOperands);
        return value;
    }

    /**
     * {@inheritDoc}
     * As the values of this property type are computed using an operator, calls
     * to this function have no effect.
     */
    @Override
    public void set(Value value) throws EPMCException {
        assert value != null;
    }

    /**
     * {@inheritDoc}
     * The type of the node property will be computed by
     * {@link Operator#resultType(Type...)} call with the results of the
     * {@link NodeProperty#getType()} of the node properties used.
     */
    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }
}
