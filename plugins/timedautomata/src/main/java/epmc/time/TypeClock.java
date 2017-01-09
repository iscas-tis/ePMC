package epmc.time;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeInteger;

public final class TypeClock implements TypeAlgebra {
	public static boolean isClock(Type type) {
		return type instanceof TypeClock;
	}
	
	public static TypeClock asClock(Type type) {
		if (isClock(type)) {
			return (TypeClock) type;
		} else {
			return null;
		}
	}
	
	private final static String CLOCK = "clock";
	/** Value context to which this type belongs. */
	private ContextValue contextValue;

	public TypeClock(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
	}
	
	@Override
	public ValueClock newValue() {
		return new ValueClock(this);
	}
	
	@Override
	public ContextValue getContext() {
		return contextValue;
	}

	@Override
	public boolean canImport(Type type) {
		assert type != null;
		if (type instanceof TypeClock) {
			return true;
		}
		if (TypeInteger.isInteger(type)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return CLOCK;
	}

	@Override
	public ValueClock getZero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueClock getOne() {
		// TODO Auto-generated method stub
		return null;
	}
		
	@Override
	public TypeArrayAlgebra getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
