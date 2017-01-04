package epmc.value;

import epmc.value.Type;

public interface TypeAlgebra extends Type {
	static boolean isAlgebra(Type type) {
		return type instanceof TypeAlgebra;
	}
	
	static TypeAlgebra asAlgebra(Type type) {
		if (isAlgebra(type)) {
			return (TypeAlgebra) type;
		} else {
			return null;
		}
	}
	
    ValueAlgebra getZero();
    
    ValueAlgebra getOne();
        
    @Override
    TypeArrayAlgebra getTypeArray();
    
    @Override
    ValueAlgebra newValue();
}
