package epmc.param.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayFunctionDAG extends ValueArrayFunction {
    private int[] content;
	private final TypeArrayFunctionDAG type;
	private boolean immutable;

    ValueArrayFunctionDAG(TypeArrayFunctionDAG type) {
    	assert type != null;
        this.type = type;
        this.content = new int[0];
    }

    @Override
    public ValueArrayFunctionDAG clone() {
    	ValueArrayFunctionDAG other = new ValueArrayFunctionDAG(getType());
    	other.set(this);
        return other;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert value instanceof ValueFunctionDAG;
        assert index >= 0;
        assert index < getTotalSize();
        content[index] = ((ValueFunctionDAG) value).getFEntry();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value instanceof ValueFunctionDAG;
        assert index >= 0;
        assert index < getTotalSize();
        ((ValueFunctionDAG) value).setEntry(content[index]);
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length < getTotalSize()) {
            content = new int[getTotalSize()];
        }
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = Arrays.hashCode(content) + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

	@Override
	public TypeArrayFunctionDAG getType() {
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
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
