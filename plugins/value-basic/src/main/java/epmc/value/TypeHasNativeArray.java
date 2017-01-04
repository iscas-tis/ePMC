package epmc.value;

import epmc.value.Type;
import epmc.value.TypeArray;

public interface TypeHasNativeArray extends Type {
	static boolean isHasNativeArray(Type type) {
		return type instanceof TypeHasNativeArray;
	}
	
	static TypeHasNativeArray asHasNativeArray(Type type) {
		if (isHasNativeArray(type)) {
			return (TypeHasNativeArray) type;
		} else {
			return null;
		}
	}
	
	static TypeArray getTypeNativeArray(Type type) {
		TypeHasNativeArray typeHasNativeArray = asHasNativeArray(type);
		if (typeHasNativeArray != null) {
			return typeHasNativeArray.getTypeArrayNative();
		} else {
			return null;
		}
	}
	
    TypeArray getTypeArrayNative();
}
