package epmc.value;

import epmc.value.Type;

public interface TypeNumBitsKnown extends Type {
	static int UNKNOWN = Integer.MAX_VALUE;
	
	static boolean isNumBitsKnown(Type type) {
		if (!(type instanceof TypeNumBitsKnown)) {
			return false;
		}
		TypeNumBitsKnown typeNumBitsKnown = (TypeNumBitsKnown) type;
		if (typeNumBitsKnown.getNumBits() == UNKNOWN) {
			return false;
		}
		return true;
	}
	
	static TypeNumBitsKnown asNumBitsKnown(Type type) {
		if (isNumBitsKnown(type)) {
			return (TypeNumBitsKnown) type;
		} else {
			return null;
		}
	}
	
	static int getNumBits(Type type) {
		TypeNumBitsKnown typeNumBitsKnown = TypeNumBitsKnown.asNumBitsKnown(type);
		if (typeNumBitsKnown != null) {
			return typeNumBitsKnown.getNumBits();
		} else {
			return UNKNOWN;
		}
	}
	
	int getNumBits();
}
