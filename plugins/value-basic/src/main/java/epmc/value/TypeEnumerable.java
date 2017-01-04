package epmc.value;

import epmc.value.Type;

public interface TypeEnumerable extends Type {
    final static int UNBOUNDED_VALUES = Integer.MAX_VALUE;

	static boolean isEnumerable(Type type) {
		if (!(type instanceof TypeEnumerable)) {
			return false;
		}
		TypeEnumerable typeEnumerable = (TypeEnumerable) type;
		if (typeEnumerable.getNumValues() == UNBOUNDED_VALUES) {
			return false;
		}
		return true;
	}
	
	static TypeEnumerable asEnumerable(Type type) {
		if (isEnumerable(type)) {
			return (TypeEnumerable) type;
		} else {
			return null;
		}
	}
	
    int getNumValues();
    
    @Override
    ValueEnumerable newValue();
}
