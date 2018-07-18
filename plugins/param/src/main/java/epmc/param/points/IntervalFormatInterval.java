package epmc.param.points;

import epmc.value.ValueInterval;
import epmc.value.ValueReal;

public final class IntervalFormatInterval implements IntervalFormat {
    public final static String IDENTIFIER = "interval";
    private final static String LEFT_BRACE = "[";
    private final static String RIGHT_BRACE = "]";
    private final static String COMMA = ",";

    @Override
    public String format(ValueInterval value, ValueFormat format) {
        assert value != null;
        assert format != null;
        ValueReal left = value.getIntervalLower();
        ValueReal right = value.getIntervalUpper();
        return LEFT_BRACE + format.format(left, Side.LEFT)
        + COMMA + format.format(right, Side.RIGHT) + RIGHT_BRACE;
    }

}
