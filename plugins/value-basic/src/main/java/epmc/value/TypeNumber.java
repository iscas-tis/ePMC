package epmc.value;

import epmc.value.Type;

public interface TypeNumber extends TypeAlgebra {
	static boolean isNumber(Type type) {
		return type instanceof TypeNumber;
	}
	
	static TypeNumber asNumber(Type type) {
		if (isNumber(type)) {
			return (TypeNumber) type;
		} else {
			return null;
		}
	}
}
