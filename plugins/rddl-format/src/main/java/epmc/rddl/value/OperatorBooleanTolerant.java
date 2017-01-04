package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public class OperatorBooleanTolerant implements Operator {
    private ContextValue context;
    private Operator internalOperator;
    private Value zero;
    private Value one;
    private Value[] operandsInternal = new Value[2];
    private Type[] typesInternal = new Type[2];
    private Type typeInteger;

    public OperatorBooleanTolerant(ContextValue context, String operatorName) {
        assert context != null;
        assert this.context == null;
        this.context = context;
        this.internalOperator = context.getOperator(operatorName);
        assert this.internalOperator != null;
        this.zero = epmc.value.UtilValue.newValue(TypeInteger.get(context), 0);
        this.one = epmc.value.UtilValue.newValue(TypeInteger.get(context), 1);
        this.typeInteger = TypeInteger.get(context);
    }
    
    @Override
    public String getIdentifier() {
    	return this.internalOperator.getIdentifier();
    }

    @Override
    public void setContext(ContextValue context) {
    }
    
    @Override
    public ContextValue getContext() {
        return this.context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	System.arraycopy(operands, 0, this.operandsInternal, 0, 2);
    	if (ValueBoolean.isBoolean(operands[0])) {
    		this.operandsInternal[0] = ValueBoolean.asBoolean(operands[0]).getBoolean() ? one : zero;
    	}
    	if (ValueBoolean.isBoolean(operands[1])) {
    		this.operandsInternal[1] = ValueBoolean.asBoolean(operands[1]).getBoolean() ? one : zero;
    	}
        this.internalOperator.apply(result, this.operandsInternal);
    }

    @Override
    public Type resultType(Type... types) {
    	System.arraycopy(types, 0, this.typesInternal, 0, 2);
    	if (TypeBoolean.isBoolean(types[0])) {
    		this.typesInternal[0] = typeInteger;
    	}
    	if (TypeBoolean.isBoolean(types[1])) {
    		this.typesInternal[1] = typeInteger;
    	}
        return internalOperator.resultType(typesInternal);
    }
    
    @Override
    public String toString() {
    	return internalOperator.toString();
    }
}
