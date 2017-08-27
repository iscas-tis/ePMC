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

import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.value.Value;

public final class ValueDouble implements ValueReal {
	public static boolean isDouble(Value value) {
		return value instanceof ValueDouble;
	}
	
	public static ValueDouble asDouble(Value value) {
		if (isDouble(value)) {
			return (ValueDouble) value;
		} else {
			return null;
		}
	}
	
    private final static String NAN = "NaN";
    private final static String SPACE = " ";
    private final static String DIVIDED = "/";
    
    private final TypeDouble type;
    private double value;
    private boolean immutable;

    ValueDouble(TypeDouble type, double value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueDouble(TypeDouble type) {
        this(type, 0.0);
    }    
    
    @Override
    public TypeDouble getType() {
        return type;
    }
    
    @Override
    public double getDouble() {
        return value;
    }

    @Override
    public void set(double value) {
        assert !isImmutable();
        this.value = value;
    }
    
    @Override
    public ValueDouble clone() {
        return new ValueDouble(getType(), value);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueDouble)) {
            return false;
        }
        ValueDouble other = (ValueDouble) obj;
        if (Double.isNaN(this.value) != Double.isNaN(other.value)) {
            return false;
        }
        if (!Double.isNaN(this.value) && this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        if (Double.isNaN(value)) {
            return -232;
        }
        if (Double.isInfinite(this.value)) {
            return -23333;
        }
        final int low = (int) Double.doubleToLongBits(value);
        final int up = (int) (Double.doubleToLongBits(value) >> 32);
        hash = low + (hash << 6) + (hash << 16) - hash;
        hash = up + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        if (Double.isNaN(value)) {
            return NAN;
        } else {
        	Options options = Options.get();
        	if (options.getBoolean(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE)) {
        		return String.valueOf(value);
        	} else {
	            String format = options.getString(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT);
	            assert format != null;
	            return String.format(format, value);
        	}
        }
    }
    
    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        double opDouble = castOrImport(op);
        this.value = opDouble;
        
    }
    
    @Override
    public void add(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        double doubleOp1 = castOrImport(op1);
        double doubleOp2 = castOrImport(op2);
        set(doubleOp1 + doubleOp2);
    }

    @Override
    public void multiply(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        double doubleOp1 = castOrImport(op1);
        double doubleOp2 = castOrImport(op2);
        set(doubleOp1 * doubleOp2);
    }

    @Override
    public void addInverse(Value op) {
        assert !isImmutable();
        assert op != null;
        double doubleOp = castOrImport(op);
        set(-doubleOp);
    }

    @Override
    public void divide(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        double doubleOp1 = castOrImport(op1);
        double doubleOp2 = castOrImport(op2);
        set(doubleOp1 / doubleOp2);
        // TODO hack to avoid NaNs in ContextDD
        if (doubleOp2 == 0.0) {
            set(0);
        }
    }

    @Override
    public void subtract(Value op1, Value op2) {
        assert !isImmutable() : this;
        assert op1 != null;
        assert op2 != null;
        double doubleOp1 = castOrImport(op1);
        double doubleOp2 = castOrImport(op2);
        set(doubleOp1 - doubleOp2);
    }

    @Override
    public double distance(Value op) throws EPMCException {
        assert op != null;
        if (!getType().canImport(op.getType())) {
            return op.distance(this);
        }
        ValueNumber opNumber = ValueNumber.asNumber(op);
        return Math.abs(value - opNumber.getDouble());
    }
    
    @Override
    public boolean isZero() {
        return getDouble() == 0.0;
    }

    @Override
    public boolean isOne() {
        return getDouble() == 1.0;
    }

    @Override
    public boolean isPosInf() {
        return getDouble() == Double.POSITIVE_INFINITY;
    }

    @Override
    public double norm() {
        return Math.abs(getDouble());
    }

    @Override
    public int getInt() {
        return (int) getDouble();
    }

    @Override
    public boolean isLt(Value operand) {
        assert operand != null;
        double op2Double = castOrImport(operand);
        return getDouble() < op2Double;
    }
    
    @Override
    public void set(int operand) {
        assert !isImmutable();
        set((double) operand);
    }
    
    private double castOrImport(Value operand) {
        assert operand != null;
        if (ValueReal.isReal(operand)) {
            return ValueNumber.asNumber(operand).getDouble();
        } else if (ValueInteger.isInteger(operand)) {
            return ValueInteger.asInteger(operand).getInt();
        } else {
            assert false : operand + SPACE + operand.getType();
            return Double.NaN;
        }
    }
    
    @Override
    public void set(String string) throws EPMCException {
        assert string != null;
        if (string.contains(DIVIDED)) {
            String[] parts = string.split(DIVIDED);
            assert parts.length == 2;
            String numString = parts[0];
            String denString = parts[1];
            double num = Double.parseDouble(numString);
            double den = Double.parseDouble(denString);
            value = num / den;
        } else {
            try {
                value = Double.parseDouble(string);
            } catch (NumberFormatException e) {
                fail(ProblemsValueBasic.VALUES_STRING_INVALID_VALUE, e, value, type);
            }
        }
        
    }
    
    @Override
    public void max(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        double doubleOp1 = castOrImport(operand1);
        double doubleOp2 = castOrImport(operand2);
        this.set(Math.max(doubleOp1, doubleOp2));
        
    }
    
    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}
}
