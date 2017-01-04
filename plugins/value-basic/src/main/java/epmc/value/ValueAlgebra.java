package epmc.value;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.value.Value;

public interface ValueAlgebra extends Value {
	@Override
	TypeAlgebra getType();
	
	static boolean isAlgebra(Value value) {
		return value instanceof ValueAlgebra;
	}
	
	static ValueAlgebra asAlgebra(Value value) {
		if (isAlgebra(value)) {
			return (ValueAlgebra) value;
		} else {
			return null;
		}
	}
	
    void set(int value);
    
    default void max(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        if (ValueAlgebra.asAlgebra(operand1).isGt(operand2)) {
            set(operand1);
        } else if (ValueAlgebra.asAlgebra(operand2).isGt(operand1)) {
            set(operand2);
        } else if (operand2.isEq(operand1)) {
            set(operand1);
        } else {
            ensure(false, ProblemsValueBasic.VALUES_INCOMPARABLE);
        }
    }
    
    default void min(Value operand1, Value operand2)
            throws EPMCException {
        assert !isImmutable();
        if (ValueAlgebra.asAlgebra(operand1).isLt(operand2)) {
            set(operand1);
        } else if (ValueAlgebra.asAlgebra(operand2).isLt(operand1)) {
            set(operand2);
        } else if (operand2.isEq(operand1)) {
            set(operand1);
        } else {
            ensure(false, ProblemsValueBasic.VALUES_INCOMPARABLE);
        }
    }
    
    void add(Value operand1, Value operand2) throws EPMCException;
    
    void divide(Value operand1, Value operand2) throws EPMCException;

    void subtract(Value operand1, Value operand2) throws EPMCException;
    
    void multiply(Value operand1, Value operand2) throws EPMCException;
    
    void addInverse(Value operand) throws EPMCException;

    void multInverse(Value operand) throws EPMCException;
    
    boolean isZero();

    boolean isOne();

    boolean isPosInf();
    
    boolean isNegInf();
    
    // TODO move
    @Override
    default int compareTo(Value other) {
        try {
            if (isEq(other)) {
                return 0;
            } else if (isLt(other)) {
                return -1;
            } else {
                assert isGt(other);
                return 1;
            }
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
    }
    
    // TODO move?
    default boolean isLt(Value other) throws EPMCException {
        UtilValue.failUnsupported(this);
        return false;
    }
    
    // TODO move?
    default boolean isLe(Value other) throws EPMCException {
        return isLt(other) || isEq(other);
    }
    
    // TODO move?
    default boolean isGe(Value other) throws EPMCException {
        return ValueAlgebra.asAlgebra(other).isLe(this);
    }

    // TODO move?
    default boolean isGt(Value other) throws EPMCException {
        return ValueAlgebra.asAlgebra(other).isLt(this);
    }    
    
    double norm() throws EPMCException;
    

    default boolean isEq(Value other) throws EPMCException {
        return distance(other) < 1E-6;
    }
}
