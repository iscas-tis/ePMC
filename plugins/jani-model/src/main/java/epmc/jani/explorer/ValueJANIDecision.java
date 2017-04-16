package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.Value;

public final class ValueJANIDecision implements Value {
	private final static String LBRACK = "(";
	private final static String RBRACK = ")";
	private final static String COMMA = ",";
	private final TypeJANIDecision type;
	private final int[] variables;
	private final Value[] values;
	private boolean immutable;

	ValueJANIDecision(TypeJANIDecision type) {
		assert type != null;
		this.type = type;
		variables = type.getVariables();
		Type[] types = type.getTypes();
		values = new Value[type.getVariables().length];
		for (int i = 0; i < types.length; i++) {
			values[i] = types[i].newValue();
		}
	}

	@Override
	public int compareTo(Value value) {
		assert value != null;
		assert value instanceof ValueJANIDecision;
		ValueJANIDecision other = (ValueJANIDecision) value;
		for (int i = 0; i < values.length; i++) {
			int cmp = values[i].compareTo(other.values[i]);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void set(Value value) {
		assert value != null;
		assert value instanceof ValueJANIDecision;
		ValueJANIDecision other = (ValueJANIDecision) value;
		for (int i = 0; i < values.length; i++) {
			values[i].set(other.values[i]);
		}
	}
	
	void set(NodeJANI node) {
		assert node != null;
		for (int index = 0; index < variables.length; index++) {
			node.getValue(values[index], variables[index]);
		}
	}

	@Override
	public void set(String value) throws EPMCException {
		assert value != null;
		value = value.trim();
		value = value.substring(1, value.length() - 1);
		value = value.trim();
		String[] strings = value.split(COMMA);
		for (int index = 0; index < values.length; index++) {
			strings[index] = strings[index].trim();
			values[index].set(strings[index]);
		}
	}

	@Override
	public void setImmutable() {
		immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		assert other != null;
		assert other instanceof ValueJANIDecision;
		return isEq(other) ? 0.0 : 1.0;
	}

	@Override
	public boolean isEq(Value other) throws EPMCException {
		assert other != null;
		assert other instanceof ValueJANIDecision;
		ValueJANIDecision otherDecision = (ValueJANIDecision) other;
		for (int index = 0; index < values.length; index++) {
			if (!values[index].isEq(otherDecision.values[index])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ValueJANIDecision)) {
			return false;
		}
		ValueJANIDecision otherDecision = (ValueJANIDecision) obj;
		for (int index = 0; index < values.length; index++) {
			if (!values[index].equals(otherDecision.values[index])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
        int hash = 0;
        for (int index = 0; index < values.length; index++) {
        	hash = values[index].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(LBRACK);
		for (int index = 0; index < values.length; index++) {
			builder.append(values[index]);
			if (index < values.length - 1) {
				builder.append(COMMA);
			}
		}

		builder.append(RBRACK);
		return builder.toString();
	}
	
	public Value[] getValues() {
		return values;
	}
}
