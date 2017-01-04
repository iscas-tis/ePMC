package epmc.value;

import epmc.value.Value;

public interface ValueEnumerable extends Value {
	static boolean isEnumerable(Value value) {
		if (!(value instanceof ValueEnumerable)) {
			return false;
		}
		ValueEnumerable valueEnumerable = (ValueEnumerable) value;
		if (valueEnumerable.getType().getNumValues() == TypeEnum.UNBOUNDED_VALUES) {
			return false;
		}
		return true;
	}
	
	static ValueEnumerable asEnumerable(Value value) {
		if (isEnumerable(value)) {
			return (ValueEnumerable) value;
		} else {
			return null;
		}
	}
	
	@Override
	TypeEnumerable getType();
	 
    int getValueNumber();

    void setValueNumber(int number);
}
