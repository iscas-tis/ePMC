package epmc.value;

import epmc.value.Type;

public interface TypeBounded extends TypeAlgebra {
	static boolean isBounded(Type type) {
		return type instanceof TypeBounded;
	}
	
	static  TypeBounded asBounded(Type type) {
		if (isBounded(type)) {
			return (TypeBounded) type;
		} else {
			return null;
		}
	}
	
	static ValueAlgebra getLower(Type type) {
		TypeBounded typeBounded = asBounded(type);
		if (typeBounded != null) {
			return typeBounded.getLower();
		} else {
			return null;
		}
	}

	static ValueAlgebra getUpper(Type type) {
		TypeBounded typeBounded = asBounded(type);
		if (typeBounded != null) {
			return typeBounded.getUpper();
		} else {
			return null;
		}
	}

    default ValueAlgebra getLower() {
        return null;
    }
    
    default ValueAlgebra getUpper() {
        return null;
    }
}
