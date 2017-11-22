package epmc.value;

public interface ValueSetString extends Value {
    static boolean is(Value value) {
        return value instanceof ValueSetString;
    }
    
    static ValueSetString as(Value value) {
        if (is(value)) {
            return (ValueSetString) value;
        } else {
            return null;
        }
    }
    
    void set(String value);
}
