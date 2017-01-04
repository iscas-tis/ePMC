package epmc.param.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueReal;

public interface ValueFunction extends ValueAlgebra, ValueReal {
    void evaluate(ValueReal result, Point point) throws EPMCException;
    
    void setParameter(Object parameter);
    
    boolean isConstant();
    
    ValueReal getConstant() throws EPMCException;
    
    @Override
    public TypeFunction getType();
    
    default ContextValuePARAM getContextPARAM() {
    	return getType().getContextPARAM();
    }
    
	ValueFunction castOrImport(Value operand, int number) throws EPMCException;

    default void apply(Operator operator, Value... operands) throws EPMCException {
    	assert operator != null;
    	assert operands != null;
    	ValueFunction[] functions = new ValueFunction[operands.length];
    	boolean allConstant = true;
    	for (int paramNr = 0; paramNr < operands.length; paramNr++) {
    		functions[paramNr] = castOrImport(operands[paramNr], paramNr);
    		if (!functions[paramNr].isConstant()) {
    			allConstant = false;
    		}
    	}
    	if (allConstant) {
    		Value[] constants = new Value[functions.length];
    		for (int paramNr = 0; paramNr < constants.length; paramNr++) {
    			constants[paramNr] = functions[paramNr].getConstant();
    		}
    		set(0);
    		operator.apply(getConstant(), constants);
    		set(getConstant());
    	} else {
    		Unevaluated unevaluated = new Unevaluated(getContextPARAM(), operator, functions);
    		setParameter(unevaluated);
    	}
    }
    
    default void apply(String operator, Value... operands) throws EPMCException {
    	assert operator != null;
    	assert operands != null;
    	apply(getType().getContext().getOperator(operator), operands);
    }
    
    @Override
    public default int floorInt() throws EPMCException {
    	assert isConstant();
    	return getConstant().floorInt();
    }

    @Override
    default int ceilInt() throws EPMCException {
    	assert isConstant();
    	return getConstant().ceilInt();
    }
    
    @Override
    public default boolean isLt(Value other) throws EPMCException {
    	assert isConstant() : this + " " + other.getClass();
    	return getConstant().isLt(other);
    }
    
    @Override
    public default boolean isGt(Value other) throws EPMCException {
    	assert false;
    	// TODO Auto-generated method stub
    	return false;
    }
    
    @Override
    public default boolean isEq(Value other) throws EPMCException {
    	assert other != null;
    	ValueFunction function = castOrImport(other, 0);
    	return equals(function);
    }
    
    @Override
    public default int signInt() throws EPMCException {
    	assert isConstant();
    	return getConstant().signInt();
    }

    @Override
    default void abs(Value operand) throws EPMCException {
    	// TODO Auto-generated method stub
    	
    }
    

	@Override
	default int intcastInt() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	default void set(double value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default void exp(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default void pow(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default void log(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default void sqrt(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default void pi() throws EPMCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	default boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	default boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	default boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	default boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	default double getDouble() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	default int getInt() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	default double norm() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	default double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}
}
