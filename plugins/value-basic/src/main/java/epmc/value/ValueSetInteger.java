package epmc.value;

public interface ValueSetInteger {
    static boolean is(Value value) {
        return value instanceof ValueSetInteger;
    }
    
    static ValueSetInteger as(Value value) {
        if (is(value)) {
            return (ValueSetInteger) value;
        } else {
            return null;
        }
    }

    void set(int value);
}
