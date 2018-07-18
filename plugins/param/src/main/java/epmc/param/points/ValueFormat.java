package epmc.param.points;

import epmc.value.ValueReal;

public interface ValueFormat {
    String format(ValueReal value, Side side);
}
