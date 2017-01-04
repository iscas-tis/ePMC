package epmc.qmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.TypeArrayReal;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;

final class ValueArrayComplex extends ValueArrayAlgebra {
    private static final long serialVersionUID = 1L;
	private final TypeArrayComplex type;
    private ValueArrayAlgebra content;
	private boolean immutable;
    
    ValueArrayComplex(TypeArrayComplex type) {
        this.type = type;
        TypeReal typeReal = TypeReal.get(getType().getContext());
        TypeArrayReal typeArrayReal = typeReal.getTypeArray();
        this.content = UtilValue.newArray(typeArrayReal, getTotalSize() * 2);
    }

    @Override
    public ValueArrayComplex clone() {
    	ValueArrayComplex clone = getType().newValue();
    	clone.set(this);
    	return clone;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert value instanceof ValueComplex;
        assert index >= 0;
        assert index < getTotalSize();
        content.set(((ValueComplex) value).getRealPart(), index * 2);
        content.set(((ValueComplex) value).getImagPart(), index * 2 + 1);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value instanceof ValueComplex;
        assert index >= 0;
        assert index < getTotalSize();
        content.get(((ValueComplex) value).getRealPart(), index * 2);
        content.get(((ValueComplex) value).getImagPart(), index * 2 + 1);
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.getTotalSize() < getTotalSize()) {
            TypeReal typeReal = TypeReal.get(getType().getContext());
            TypeArrayReal typeArrayReal = typeReal.getTypeArray();
            content = UtilValue.newArray(typeArrayReal, getTotalSize() * 2);
        }
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public TypeArrayComplex getType() {
		return type;
	}
    
    @Override
    public void setImmutable() {
    	immutable = true;
    }
    
    @Override
    public boolean isImmutable() {
    	return immutable;
    }

	@Override
	public void set(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
