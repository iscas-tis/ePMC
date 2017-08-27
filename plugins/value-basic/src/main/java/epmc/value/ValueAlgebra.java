/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

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
                assert isGt(other) : this + " " + other;
                return 1;
            }
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
    }
    
    // TODO move?
    default boolean isLt(Value other) throws EPMCException {
    	assert false;
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
    

    @Override
	default boolean isEq(Value other) throws EPMCException {
        return distance(other) < 1E-6;
    }
}
