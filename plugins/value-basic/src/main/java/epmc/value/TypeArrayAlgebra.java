package epmc.value;

import epmc.value.Type;
import epmc.value.TypeArray;

public interface TypeArrayAlgebra extends TypeArray, TypeAlgebra {
	static boolean isArrayAlgebra(Type type) {
		return type instanceof TypeArrayAlgebra;
	}
	
	static TypeArrayAlgebra asArrayAlgebra(Type type) {
		if (isArrayAlgebra(type)) {
			return (TypeArrayAlgebra) type;
		} else {
			return null;
		}
	}
	
	@Override
	TypeAlgebra getEntryType();
	
    @Override
    ValueArrayAlgebra newValue();
    
    @Override
    default ValueAlgebra getZero() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    default ValueAlgebra getOne() {
    	// TODO Auto-generated method stub
    	return null;
    }
}
