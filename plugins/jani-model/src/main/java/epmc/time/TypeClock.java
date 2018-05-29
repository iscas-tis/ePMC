package epmc.time;

import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;

public final class TypeClock implements TypeAlgebra {
    public static boolean is(Type type) {
        return type instanceof TypeClock;
    }

    public static TypeClock as(Type type) {
        if (is(type)) {
            return (TypeClock) type;
        } else {
            return null;
        }
    }

    private final static String CLOCK = "clock";

    @Override
    public ValueClock newValue() {
        return new ValueClock(this);
    }

    @Override
    public String toString() {
        return CLOCK;
    }

    @Override
    public TypeArrayAlgebra getTypeArray() {
        // TODO Auto-generated method stub
        return null;
    }
}
