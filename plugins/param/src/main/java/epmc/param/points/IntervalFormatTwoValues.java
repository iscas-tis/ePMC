package epmc.param.points;

import epmc.value.ValueInterval;
import epmc.value.ValueReal;

public final class IntervalFormatTwoValues implements IntervalFormat {
    public final static String IDENTIFIER = "two-values";
    private final static String SPACE = " ";

    @Override
    public String format(ValueInterval value, ValueFormat format) {
        assert value != null;
        assert format != null;
        ValueReal left = value.getIntervalLower();
        ValueReal right = value.getIntervalUpper();
        return format.format(left, Side.LEFT)
                + SPACE + format.format(right, Side.RIGHT);
    }
}
