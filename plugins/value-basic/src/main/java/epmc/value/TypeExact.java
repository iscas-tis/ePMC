package epmc.value;

import epmc.value.Type;

public interface TypeExact extends Type {
	static boolean isExact(Type type) {
		return type instanceof TypeExact;
	}
}
