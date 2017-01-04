package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

public abstract class ValueArrayAlgebra extends ValueArray implements ValueAlgebra {
	public static boolean isArrayAlgebra(Value value) {
		return value instanceof ValueArrayAlgebra;
	}
	
	public static ValueArrayAlgebra asArrayAlgebra(Value value) {
		if (isArrayAlgebra(value)) {
			return (ValueArrayAlgebra) value;
		} else {
			return null;
		}
	}
	
    private ValueArrayAlgebra resultBuffer;
    private ValueAlgebra[] entryAccs = new ValueAlgebra[1];

    protected ValueAlgebra getEntryAcc(int number) {
        assert number >= 0;
        int numEntryAccs = this.entryAccs.length;
        while (number >= numEntryAccs) {
            numEntryAccs *= 2;
        }
        if (numEntryAccs != this.entryAccs.length) {
            this.entryAccs = Arrays.copyOf(this.entryAccs, numEntryAccs);
        }
        if (this.entryAccs[number] == null) {
            this.entryAccs[number] = getType().getEntryType().newValue();
        }
        return this.entryAccs[number];
    }
    

    @Override
    public void add(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        ValueArray op1Array = castOrImport(op1, 0, false);
        ValueArray op2Array = castOrImport(op2, 1, false);
        setDimensions(op1Array.getDimensions());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            op2Array.get(getEntryAcc(1), index);
            getEntryAcc(2).add(getEntryAcc(0), getEntryAcc(1));
            set(getEntryAcc(2), index);
        }
    }

    @Override
    public void addInverse(Value op) throws EPMCException {
        assert !isImmutable();
        ValueArray opArray = castOrImport(op, 0, false);
        setDimensions(opArray.getDimensions());
        for (int index = 0; index < opArray.getTotalSize(); index++) {
            opArray.get(getEntryAcc(0), index);
            getEntryAcc(2).addInverse(getEntryAcc(0));
            set(getEntryAcc(2), index);
        }
    }

    @Override
    public void subtract(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        ValueArray op1Array = castOrImport(op1, 0, false);
        ValueArray op2Array = castOrImport(op2, 0, false);
        setDimensions(op1Array.getDimensions());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            op2Array.get(getEntryAcc(1), index);
            getEntryAcc(2).subtract(getEntryAcc(0), getEntryAcc(1));
            set(getEntryAcc(2), index);
        }
    }

    @Override
    public void multiply(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        if (!isArray(op2)) {
            multiplyByConstant(op1, op2);
        } else {
            matrixMultiply(castOrImport(op1, 0, true), castOrImport(op2, 1, true));
        }
    }
    
    @Override
    public void multInverse(Value op) throws EPMCException {
        assert !isImmutable();
        matrixMultInverse(castOrImport(op, 0, true));
    }

    @Override
    public void divide(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        if (!isArray(op2)) {
            divideByConstant(op1, op2);
        } else {
            assert false;
        }
    }

    private ValueArray divideByConstant(Value op1, Value op2) throws EPMCException {
        ValueArray op1Array = castOrImport(op1, 0, false);
        setDimensions(op1Array.getDimensions());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            getEntryAcc(1).divide(getEntryAcc(0), op2);
            set(getEntryAcc(1), index);
        }
        return this;
    }

    private ValueArray multiplyByConstant(Value op1, Value op2) throws EPMCException {
        ValueArray op1Array = castOrImport(op1, 0, false);
        setDimensions(op1Array.getDimensions());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            getEntryAcc(1).divide(getEntryAcc(0), op2);
            set(getEntryAcc(1), index);
        }
        return this;
    }
    
    public ValueArray matrixMultiply(ValueArray op1, ValueArray op2) throws EPMCException {
        assert !isImmutable();
        if (this == op1 || this == op2) {
            if (resultBuffer == null) {
                resultBuffer = (ValueArrayAlgebra) getType().newValue();
            }
            resultBuffer.matrixMultiply(op1, op2);
            set(resultBuffer);
        }
        int[] dimensions = {op1.getLength(0), op2.getLength(1)};
        setDimensions(dimensions);
        for (int row = 0; row < op1.getLength(0); row++) {
            for (int column = 0; column < op2.getLength(1); column++) {
                getEntryAcc(3).set(getType().getEntryType().getZero());
                for (int inner = 0; inner < op1.getLength(1); inner++) {
                    op1.get(getEntryAcc(0), row, inner);
                    op2.get(getEntryAcc(1), inner, column);
                    getEntryAcc(2).multiply(getEntryAcc(0), getEntryAcc(1));
                    getEntryAcc(0).add(getEntryAcc(2), getEntryAcc(3));
                    getEntryAcc(3).set(getEntryAcc(0));
                }
                set(getEntryAcc(3), row, column);
            }            
        }
        return this;
    }
    
    public ValueArray matrixMultInverse(ValueArray op) {
        assert !isImmutable();
        assert false;
        return this;
    }
    
    public final void set(int entry, int... indices) {
        assert !isImmutable();
        getEntryAcc(0).set(entry);
        set(getEntryAcc(0), indices);
    }
    
    @Override
    public double norm() throws EPMCException {
        assert !isImmutable();
        double result = 0.0;
        for (int index = 0; index < getTotalSize(); index++) {
            get(getEntryAcc(0), index);
            Math.max(result, getEntryAcc(0).norm());
        }
        return result;
    }
    
    @Override
    public abstract TypeArrayAlgebra getType();
    
    @Override
    public abstract ValueArrayAlgebra clone();
}
