package epmc.time;

import epmc.value.Value;
import epmc.value.ValueAlgebra;

public final class ValueClock implements ValueAlgebra {
    private TypeClock type;
    private int value;

    ValueClock(TypeClock type) {
        assert false;
        this.type = type;
    }

    @Override
    public ValueClock clone() {
        ValueClock clone = type.newValue();
        clone.set(value);
        return clone;
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
        return "valueClock(" + value + ")";
    }

    @Override
    public void set(Value value) {
        // TODO Auto-generated method stub

    }
}
