package epmc.value;

public final class UtilTernary {
    public static Ternary getTernary(Value value) {
        assert value != null;
        assert ValueBoolean.isBoolean(value) || ValueTernary.isTernary(value);
        if (ValueBoolean.isBoolean(value)) {
            return ValueBoolean.asBoolean(value).getBoolean() ? Ternary.TRUE : Ternary.FALSE;
        } else if (ValueTernary.isTernary(value)) {
            return ValueTernary.asTernary(value).getTernary();
        } else {
            assert false;
            return null;
        }
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilTernary() {
    }
}
