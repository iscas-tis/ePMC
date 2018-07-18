package epmc.param.points;

import epmc.value.ValueInterval;

public interface IntervalFormat {
    String format(ValueInterval value, ValueFormat format);
}
