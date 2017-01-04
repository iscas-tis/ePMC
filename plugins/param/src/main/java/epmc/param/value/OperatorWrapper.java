package epmc.param.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class OperatorWrapper implements Operator {
	private final static String WRAPPER = "wrapper";
	private final static String LBRACE = "(";
	private final static String RBRACE = ")";
	
	private final ContextValuePARAM contextValuePARAM;
	private final Operator wrapped;

	public OperatorWrapper(ContextValuePARAM contextValuePARAM, Operator wrapped) {
		assert contextValuePARAM != null;
		assert wrapped != null;
		this.contextValuePARAM = contextValuePARAM;
		this.wrapped = wrapped;
	}
	
	@Override
	public String getIdentifier() {
		return this.wrapped.getIdentifier();
	}

	@Override
	public void setContext(ContextValue context) {
		this.wrapped.setContext(context);
	}

	@Override
	public ContextValue getContext() {
		return this.wrapped.getContext();
	}

	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
    	assert operands != null;
		if ((result instanceof ValueFunction)
				&& !getTypeFunction().isSupportOperator(getIdentifier())) {
	    	ValueFunction resultFunction = (ValueFunction) result;
	    	ValueFunction[] functions = new ValueFunction[operands.length];
	    	boolean allConstant = true;
	    	for (int paramNr = 0; paramNr < operands.length; paramNr++) {
	    		functions[paramNr] = resultFunction.castOrImport(operands[paramNr], paramNr);
	    		if (!functions[paramNr].isConstant()) {
	    			allConstant = false;
	    		}
	    	}
	    	if (allConstant) {
	    		Value[] constants = new Value[functions.length];
	    		for (int paramNr = 0; paramNr < constants.length; paramNr++) {
	    			constants[paramNr] = functions[paramNr].getConstant();
	    		}
	    		resultFunction.set(0);
	    		wrapped.apply(resultFunction.getConstant(), constants);
	    		resultFunction.set(resultFunction.getConstant());
	    	} else {
	    		Unevaluated unevaluated = new Unevaluated(contextValuePARAM, this, functions);
	    		resultFunction.setParameter(unevaluated);
	    	}
		} else {
			this.wrapped.apply(result, operands);
		}
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		boolean foundFunction = false;
		for (Type type : types) {
			if (type instanceof TypeFunction) {
				foundFunction = true;
				break;
			}
		}
		if (foundFunction) {
			return this.contextValuePARAM.getTypeFunction();
		} else {
			return this.wrapped.resultType(types);
		}
	}

	private TypeFunction getTypeFunction() {
		return this.contextValuePARAM.getTypeFunction();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(WRAPPER + LBRACE);
		result.append(getIdentifier());
		result.append(RBRACE);
		return result.toString();
	}
}
