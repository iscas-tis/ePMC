package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.ValueAlgebra;

public final class TypeMatrix implements TypeAlgebra {
    public final static class Builder {
        private TypeArrayAlgebra typeArray;
        private int numRows = UNKNOWN;
        private int numColumns = UNKNOWN;

        public Builder setTypeArray(TypeArrayAlgebra typeArray) {
            this.typeArray = typeArray;
            return this;
        }
        
        private TypeArrayAlgebra getTypeArray() {
            return typeArray;
        }
        
        public Builder setNumRows(int numRows) {
            this.numRows = numRows;
            return this;
        }

        private int getNumRows() {
            return numRows;
        }
        
        public Builder setNumColumns(int numColumns) {
            this.numColumns = numColumns;
            return this;
        }
        
        private int getNumColumns() {
            return numColumns;
        }
        
        public TypeMatrix build() {
            ContextValue contextValue = typeArray.getContext();
            return contextValue.makeUnique(new TypeMatrix(this));
        }
    }
    
    private final static int UNKNOWN = -1;
    private static final long serialVersionUID = 1L;
    private final TypeArrayAlgebra typeArray;
    private int numRows;
    private int numColumns;

    private TypeMatrix(Builder builder) {
        assert builder != null;
        assert builder.getTypeArray() != null;
        assert (builder.getNumRows() == UNKNOWN)
            == (builder.getNumColumns() == UNKNOWN);
        assert builder.getNumRows() == UNKNOWN
                || builder.getNumRows() >= 0;
        this.typeArray = builder.getTypeArray();
        this.numRows = builder.getNumRows();
        this.numColumns = builder.getNumColumns();
    }
    
    @Override
    public ContextValue getContext() {
        return typeArray.getContext();
    }

    @Override
    public ValueMatrix newValue() {
        return new ValueMatrix(this);
    }

    public ValueMatrix newValue(int numRows, int numColumns) {
    	ValueMatrix result = newValue();
    	result.setDimensions(numRows, numColumns);
    	return result;
    }
    
    public int getNumRows() {
        return numRows;
    }
    
    public int getNumColumns() {
        return numColumns;
    }

    public TypeArray getArrayType() {
        return typeArray;
    }
    
    public TypeAlgebra getEntryType() {
        return typeArray.getEntryType();
    }
    
    @Override
    public ValueAlgebra getOne() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public ValueAlgebra getZero() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
	@Override
    public TypeArrayAlgebra getTypeArray() {
		return null;
		// TODO
//        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
	
	@Override
	public boolean canImport(Type type) {
        assert type != null;
        if (this == type) {
            return true;
        }
        return false;
	}
}
