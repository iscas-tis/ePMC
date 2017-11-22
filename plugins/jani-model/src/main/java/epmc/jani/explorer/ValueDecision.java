package epmc.jani.explorer;

import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueSetString;

public final class ValueDecision implements Value, ValueSetString {
    private final static String LBRACE = "(";
    private final static String RBRACE = ")";
    private final static String COMMA = ",";
    private final TypeDecision type;
    private final int[] variables;
    private final Value[] values;

    public static boolean is(Value value) {
        return value instanceof ValueDecision;
    }
    
    public static ValueDecision as(Value value) {
        if (is(value)) {
            return (ValueDecision) value;
        } else {
            return null;
        }
    }
    
    ValueDecision(TypeDecision type) {
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
    public Type getType() {
        return type;
    }

    void set(NodeJANI node) {
        assert node != null;
        for (int index = 0; index < variables.length; index++) {
            node.getValue(values[index], variables[index]);
        }
    }

    @Override
    public void set(String value) {
        assert value != null;
        value = value.trim();
        value = value.substring(1, value.length() - 1);
        value = value.trim();
        String[] strings = value.split(COMMA);
        for (int index = 0; index < values.length; index++) {
            strings[index] = strings[index].trim();
            ValueSetString.as(values[index]).set(strings[index]);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueDecision)) {
            return false;
        }
        ValueDecision otherDecision = (ValueDecision) obj;
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
        builder.append(LBRACE);
        for (int index = 0; index < values.length; index++) {
            builder.append(values[index]);
            if (index < values.length - 1) {
                builder.append(COMMA);
            }
        }

        builder.append(RBRACE);
        return builder.toString();
    }

    public Value[] getValues() {
        return values;
    }
}
