package epmc.param.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorDivide;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.Ternary;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.TypeTernary;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueNumBitsKnown;
import epmc.value.ValueReal;

final class ValueFunctionRegionMapped implements ValueFunction, ValueNumBitsKnown {
    private int entry;
    private ValueFunctionRegionMapped importValues[]
            = new ValueFunctionRegionMapped[0];
    private TypeFunctionRegionMapped type;
    private boolean immutable;
    private final TypeTernary typeTernary;
    
    ValueFunctionRegionMapped(TypeFunctionRegionMapped type) {
        this.type = type;
        entry = -1;
        typeTernary = type.getContext().makeUnique(new TypeTernary(type.getContext()));
    }

    ValueFunctionRegionMapped(ValueFunctionRegionMapped other) {
        this(other.getType());
    }
        
    @Override
    public ValueFunctionRegionMapped clone() {
        return new ValueFunctionRegionMapped(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueFunctionRegionMapped)) {
            return false;
        }
        ValueFunctionRegionMapped other = (ValueFunctionRegionMapped) obj;
        return this.entry == other.entry;
    }

    @Override
    public int hashCode() {
        return entry;
    }

    @Override
    public String toString() {
        return getType().mapToString(entry);
    }
    
    @Override
    public TypeFunctionRegionMapped getType() {
        return type;
    }

    @Override
    public void set(Value operand) {
        assert !isImmutable();
        this.entry = opEntry(operand, 0);
        
    }

    @Override
    public void add(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorAdd.IDENTIFIER, opEntry(operand1, 0), opEntry(operand2, 1));
        
    }
    
    @Override
    public void addInverse(Value operand) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorAddInverse.IDENTIFIER, opEntry(operand, 0));
        
    }
    
    @Override
    public void divide(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorDivide.IDENTIFIER, opEntry(operand1, 0), opEntry(operand2, 1));
        
    }
    
    @Override
    public void max(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorMax.IDENTIFIER, opEntry(operand1, 0), opEntry(operand2, 1));
        
    }
    
    @Override
    public void min(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorMin.IDENTIFIER, opEntry(operand1, 0), opEntry(operand2, 1));
        
    }
    
    @Override
    public void multInverse(Value operand) throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorMultiplyInverse.IDENTIFIER, opEntry(operand, 0));
        
    }
    
    @Override
    public void multiply(Value operand1, Value operand2)
            throws EPMCException {
        assert !isImmutable();
        this.entry = apply(OperatorMultiply.IDENTIFIER, opEntry(operand1, 0), opEntry(operand2, 1));
        
    }
    
    @Override
    public void set(int valueInt) {
        assert !isImmutable();
        Value value = UtilValue.newValue(TypeInteger.get(getType().getContext()), valueInt);
        this.entry = opEntry(value, 0);
        
    }
    
    public void set(Ternary ternary) {
        assert !isImmutable();
        assert ternary != null;
        TypeTernary typeTernary = getTypeTernary();
        Value value;
        switch (ternary) {
        case FALSE:
            value = typeTernary.getFalse();
            break;
        case TRUE:
            value = typeTernary.getTrue();
            break;
        case UNKNOWN:
            value = typeTernary.getUnknown();
            break;
        default:
            assert false;
            value = null;
            break;
        }
        this.entry = opEntry(value, 0);
        
    }
    
    public void set(Object content) {
        assert !isImmutable();
        assert content != null;
        TypeObject typeObject = 
        		new TypeObject.Builder()
                .setContext(getType().getContext())
                .setClazz(content.getClass())
                .build();
        this.entry = opEntry(typeObject.newValue(content), 0);
        
    }

    private int apply(String name, int...operands) {
        // TODO
        return -1;
    }
    
    private int apply(Operator operator, int...operands) {
        // TODO
        return -1;
//        return getType().apply(operator, operands);
    }
        
    private int opEntry(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        if (operand instanceof ValueFunctionRegionMapped) {
            return ((ValueFunctionRegionMapped) operand).entry;
        } else {
            if (importValues.length <= number) {
                importValues = Arrays.copyOf(importValues, number + 1);
            }
            if (importValues[number] == null) {
                importValues[number] = getType().newValue();
            }
            importValues[number].entry = getType().newRegion(operand);
            return importValues[number].entry;
        }
    }

    @Override
    public void evaluate(ValueReal result, Point point) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return this.immutable;
    }

	@Override
	public int compareTo(Value o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setParameter(Object parameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueReal getConstant() {
		return null;
		// TODO Auto-generated method stub
		
	}

	private TypeTernary getTypeTernary() {
		return typeTernary;
	}
	
	@Override
	public ValueFunction castOrImport(Value operand, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subtract(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
    
}
