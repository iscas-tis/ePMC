package epmc.param.points;

import epmc.value.ValueReal;

public final class ValueFormatDouble implements ValueFormat {
    public final static String IDENTIFIER = "double";
    
    @Override
    public String format(ValueReal value, Side side) {
        assert value != null;
        assert side != null;
        double doubleValue = UtilPoints.realToDouble(value, side);
        return Double.toString(doubleValue);
    }

}
