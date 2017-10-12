package epmc.time;

import epmc.value.ValueAlgebra;

public final class ValueClock implements ValueAlgebra {
    private final static String TO_STRING_TEMPLATE = "valueClock(%d)";
    private TypeClock type;
    private int value;

    ValueClock(TypeClock type) {
        assert false;
        this.type = type;
    }

    @Override
    public TypeClock getType() {
        return type;
    }

    @Override
    public void set(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_TEMPLATE, value);
    }
}
