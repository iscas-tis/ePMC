package epmc.value;

public interface TypeTrigonometric extends TypeAlgebra {
	static boolean isTrigonometric(Type type) {
		return type instanceof TypeTrigonometric;
	}
	
	static TypeTrigonometric asTrigonometric(Type type) {
		if (isTrigonometric(type)) {
			return (TypeTrigonometric) type;
		} else {
			return null;
		}
	}
}
