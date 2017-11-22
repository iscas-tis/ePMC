package epmc.value;

public final class UtilTernary {
    public static Ternary getTernary(Value value) {
        assert value != null;
        assert ValueBoolean.is(value) || ValueTernary.is(value);
        if (ValueBoolean.is(value)) {
            return ValueBoolean.as(value).getBoolean() ? Ternary.TRUE : Ternary.FALSE;
        } else if (ValueTernary.is(value)) {
            return ValueTernary.as(value).getTernary();
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
